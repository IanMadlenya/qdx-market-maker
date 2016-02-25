package net.quedex.marketmaker;

import com.google.common.base.MoreObjects;
import net.quedex.api.entities.Instrument;
import net.quedex.api.entities.InstrumentType;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Pricing {

    private static final long YEAR_MILLIS = 1000L * 60 * 60 * 24 * 365;
    private static final double THRESHOLD = Math.pow(10, -8);

    private static final NormalDistribution STD_NORMAL = new NormalDistribution();

    private final TimeProvider timeProvider;
    private final double beta;
    private final double volOfVol;
    private final double rho;
    private final boolean useTimeAdjustedVolOfVol;

    public Pricing(
            TimeProvider timeProvider,
            double beta,
            double volOfVol,
            double rho,
            boolean useTimeAdjustedVolOfVol
    ) {
        checkArgument(0 <= beta && beta <= 1, "beta=%s outside [0, 1]", beta);
        checkArgument(volOfVol >= 0, "volOfvol=%s < 0", volOfVol);
        checkArgument(-1 < rho && rho < 1, "rho=%s outside (-1, 1)", rho);
        this.timeProvider = checkNotNull(timeProvider, "null timeProvider");
        this.beta = beta;
        this.volOfVol = volOfVol;
        this.rho = rho;
        this.useTimeAdjustedVolOfVol = useTimeAdjustedVolOfVol;
    }

    public Metrics calculateMetrics(Instrument instrument, double volatility, double futuresPrice) {
        if (instrument.getInstrumentType() == InstrumentType.FUTURES) {
            return new Metrics(futuresPrice, 1, 0, 0, 0);
        } else {
            double timeToMaturity = yearsToMaturity(instrument.getExpirationDate());
            double strike = instrument.getStrike().get().doubleValue();
            return black76(
                    instrument.getInstrumentType(),
                    getSABRImpliedVolatility(volatility, futuresPrice, timeToMaturity, strike),
                    futuresPrice,
                    timeToMaturity,
                    strike
            );
        }
    }

    /**
     * Based on http://www.riskencyclopedia.com/articles/black_1976/
     */
    private Metrics black76(InstrumentType type, double s, double f, double t, double x) {

        final double sqrtT = Math.sqrt(t);
        final double d1 = (Math.log(f / x) + (s * s / 2) * t) / (s * sqrtT);
        final double d2 = d1 - s * sqrtT;

        final double cdfD1 = STD_NORMAL.cumulativeProbability(d1);
        final double densityD1 = STD_NORMAL.density(d1);
        double delta = cdfD1;
        final double gamma = densityD1 / (f * s * sqrtT);
        final double gammaP = gamma * f / 100;
        final double vega = f * densityD1 * sqrtT / 100;
        final double theta = (-(f * densityD1 * s) / (2 * sqrtT)) / 365.0;

        double price;
        if (type == InstrumentType.OPTION_EUROPEAN_CALL) {
            price = f * cdfD1 - x * STD_NORMAL.cumulativeProbability(d2);
        } else {
            price = x * STD_NORMAL.cumulativeProbability(-d2) - f * (1 - cdfD1);
            delta = delta - 1; // PUT-CALL parity
        }

        if (price < 0) { // may happen with very OTM options
            price = 0;
        }

        return new Metrics(price, delta, gammaP, vega, theta);
    }

    /**
     * Based on http://www.math.ku.dk/~rolf/SABR.pdf
     */
    private double getSABRImpliedVolatility(
            double volatility,
            double futuresPrice,
            double timeToMaturity,
            double strike
    ) {
        double volOfVol = useTimeAdjustedVolOfVol ? this.volOfVol * Math.sqrt(1 / timeToMaturity) : this.volOfVol;

        double z = volOfVol / volatility
                * Math.pow(futuresPrice * strike, 0.5 * (1 - beta))
                * Math.log(futuresPrice / strike);
        double x = Math.log((Math.sqrt(1 - 2 * rho * z + Math.pow(z, 2)) + z - rho) / (1 - rho));

        double numerator = 1 + (
                Math.pow(1 - beta, 2) / 24 * Math.pow(volatility, 2) / Math.pow(futuresPrice * strike, 1 - beta)
                + 0.25 * rho * beta * volOfVol * volatility / Math.pow(futuresPrice * strike, 0.5 * (1 - beta))
                + (2 - 3 * Math.pow(rho, 2)) * volOfVol
        ) * timeToMaturity;

        if (Math.abs((futuresPrice - strike) / futuresPrice) < THRESHOLD) {
            return volatility * numerator / Math.pow(futuresPrice, 1 - beta);
        } else {
            double denominator = x * Math.pow(futuresPrice * strike, 0.5 * (1 - beta)) * (
                    1 + Math.pow((1 - beta) * Math.log(futuresPrice / strike), 2) / 24
                    + Math.pow((1 - beta) * Math.log(futuresPrice / strike), 4) / 1920
            );

            return z * volatility * numerator / denominator;
        }
    }

    private double yearsToMaturity(long expirationDate) {
        long now = timeProvider.getCurrentTime();
        return BigDecimal.valueOf(expirationDate - now)
                .divide(BigDecimal.valueOf(YEAR_MILLIS), 10, BigDecimal.ROUND_UP)
                .doubleValue();
    }

    public static final class Metrics {

        private final double price;
        private final double delta;
        private final double gammaP;
        private final double vega;
        private final double theta;

        public Metrics(double price, double delta, double gammaP, double vega, double theta) {
            checkArgument(price >= 0, "price=%s < 0", price);
            checkArgument(-1 <= delta && delta <= 1, "delta=%s outside [-1, 1]", delta);
            this.price = price;
            this.delta = delta;
            this.gammaP = gammaP;
            this.vega = vega;
            this.theta = theta;
        }

        public double getPrice() {
            return price;
        }

        public double getDelta() {
            return delta;
        }

        public double getGammaP() {
            return gammaP;
        }

        public double getVega() {
            return vega;
        }

        public double getTheta() {
            return theta;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("price", price)
                    .add("delta", delta)
                    .add("gammaP", gammaP)
                    .add("vega", vega)
                    .add("theta", theta)
                    .toString();
        }
    }
}
