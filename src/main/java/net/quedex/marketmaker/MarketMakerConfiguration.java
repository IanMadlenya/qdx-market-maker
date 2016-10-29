package net.quedex.marketmaker;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

public class MarketMakerConfiguration
{
    private final int timeSleepSeconds;

    private final BigDecimal futuresSpreadFraction;
    private final BigDecimal fairPriceSensitivityFraction;

    private final double fairVolatility;
    private final double volatilitySpreadFraction;
    private final double sabrBeta;
    private final double sabrVolOfVol;
    private final double sabrRho;
    private final boolean sabrUseTimeAdjustedVolOfVol;

    private final int numLevels;
    private final int qtyOnLevel;

    private final double vegaLimit;
    private final double deltaLimit;

    public MarketMakerConfiguration(
        final int timeSleepSeconds,
        final BigDecimal futuresSpreadFraction,
        final BigDecimal fairPriceSensitivityFraction,
        final double fairVolatility,
        final double volatilitySpreadFraction,
        final double sabrBeta,
        final double sabrVolOfVol,
        final double sabrRho,
        final boolean sabrUseTimeAdjustedVolOfVol,
        final int numLevels,
        final int qtyOnLevel,
        final double deltaLimit,
        final double vegaLimit)
    {
        checkArgument(timeSleepSeconds > 0, "timeSleepSeconds=%s <= 0", timeSleepSeconds);
        checkArgument(
            futuresSpreadFraction.compareTo(BigDecimal.ZERO) > 0,
            "futuresSpreadFraction=%s <=0", futuresSpreadFraction
        );
        checkArgument(
            fairPriceSensitivityFraction.compareTo(BigDecimal.ZERO) > 0,
            "fairPriceSensitivityFraction=%s <=0", fairPriceSensitivityFraction
        );
        checkArgument(fairVolatility > 0, "fairVolatility=%s <= 0", fairVolatility);
        checkArgument(volatilitySpreadFraction > 0, "volatilitySpreadFraction=%s <= 0", volatilitySpreadFraction);
        checkArgument(0 <= sabrBeta && sabrBeta <= 1, "sabrBeta=%s outside [0, 1]", sabrBeta);
        checkArgument(sabrVolOfVol >= 0, "sabrVolOfVol=%s < 0", sabrVolOfVol);
        checkArgument(-1 < sabrRho && sabrRho < 1, "rho=%s outside (-1, 1)", sabrRho);
        checkArgument(numLevels > 0, "numLevels=%s <= 0", numLevels);
        checkArgument(qtyOnLevel > 0, "qtyOnLevel=%s <= 0", qtyOnLevel);
        checkArgument(deltaLimit >= 0, "deltaLimit=%s < 0", deltaLimit);
        checkArgument(vegaLimit >= 0, "vegaLimit=%s < 0", vegaLimit);
        checkArgument(
            fairVolatility - numLevels * fairVolatility * volatilitySpreadFraction > 0,
            "Nonpositive lowest level volatility"
        );

        this.timeSleepSeconds = timeSleepSeconds;
        this.futuresSpreadFraction = futuresSpreadFraction;
        this.fairPriceSensitivityFraction = fairPriceSensitivityFraction;
        this.fairVolatility = fairVolatility;
        this.volatilitySpreadFraction = volatilitySpreadFraction;
        this.vegaLimit = vegaLimit;
        this.sabrBeta = sabrBeta;
        this.sabrVolOfVol = sabrVolOfVol;
        this.sabrRho = sabrRho;
        this.sabrUseTimeAdjustedVolOfVol = sabrUseTimeAdjustedVolOfVol;
        this.numLevels = numLevels;
        this.qtyOnLevel = qtyOnLevel;
        this.deltaLimit = deltaLimit;
    }

    public static MarketMakerConfiguration fromPropertiesFile(final String fileName) throws ConfigurationException
    {
        final Configuration configuration = new PropertiesConfiguration(fileName);
        return new MarketMakerConfiguration(
            configuration.getInt(ConfigKey.TIME_SLEEP_SECONDS.getKey()),
            new BigDecimal(configuration.getString(ConfigKey.SPREAD_FRACTION.getKey())),
            new BigDecimal(configuration.getString(ConfigKey.FAIR_PRICE_SENSITIVITY_FRACTION.getKey())),
            configuration.getDouble(ConfigKey.FAIR_VOLATILITY.getKey()),
            configuration.getDouble(ConfigKey.VOLATILITY_SPREAD_FRACTION.getKey()),
            configuration.getDouble(
                ConfigKey.SABR_BETA.getKey()),
            configuration.getDouble(ConfigKey.SABR_VOL_OF_VOL.getKey()),
            configuration.getDouble(ConfigKey.SABR_RHO.getKey()),
            configuration.getBoolean(ConfigKey.SABR_USE_TIME_ADJUSTED_VOL_OF_VOL.getKey()),
            configuration.getInt(ConfigKey.NUM_LEVELS.getKey()),
            configuration.getInt(ConfigKey.QUANTITY_ON_LEVEL.getKey()),
            configuration.getDouble(ConfigKey.DELTA_LIMIT.getKey()),
            configuration.getDouble(ConfigKey.VEGA_LIMIT.getKey()
            )
        );
    }

    public int getTimeSleepSeconds()
    {
        return timeSleepSeconds;
    }

    public BigDecimal getFuturesSpreadFraction()
    {
        return futuresSpreadFraction;
    }

    public BigDecimal getFairPriceSensitivityFraction()
    {
        return fairPriceSensitivityFraction;
    }

    public double getFairVolatility()
    {
        return fairVolatility;
    }

    public double getVolatilitySpreadFraction()
    {
        return volatilitySpreadFraction;
    }

    public double getVegaLimit()
    {
        return vegaLimit;
    }

    public double getSabrBeta()
    {
        return sabrBeta;
    }

    public double getSabrVolOfVol()
    {
        return sabrVolOfVol;
    }

    public double getSabrRho()
    {
        return sabrRho;
    }

    public boolean useSabrTimeAdjustedVolOfVol()
    {
        return sabrUseTimeAdjustedVolOfVol;
    }

    public int getNumLevels()
    {
        return numLevels;
    }

    public int getQtyOnLevel()
    {
        return qtyOnLevel;
    }

    public double getDeltaLimit()
    {
        return deltaLimit;
    }

    private enum ConfigKey
    {
        TIME_SLEEP_SECONDS("timeSleepSeconds"),
        SPREAD_FRACTION("futuresSpreadFraction"),
        FAIR_PRICE_SENSITIVITY_FRACTION("fairPriceSensitivityFraction"),
        FAIR_VOLATILITY("fairVolatility"),
        VOLATILITY_SPREAD_FRACTION("volatilitySpreadFraction"),
        SABR_BETA("sabr.beta"),
        SABR_VOL_OF_VOL("sabr.volOfVol"),
        SABR_RHO("sabr.rho"),
        SABR_USE_TIME_ADJUSTED_VOL_OF_VOL("sabr.useTimeAdjustedVolOfVol"),
        NUM_LEVELS("numLevels"),
        QUANTITY_ON_LEVEL("quantityOnLevel"),
        VEGA_LIMIT("vegaLimit"),
        DELTA_LIMIT("deltaLimit");

        private static final String COMMON_PREFIX = "net.quedex.marketmaker";
        private static final char SEPARATOR = '.';

        public static String getCommonPrefix()
        {
            return COMMON_PREFIX;
        }

        private final String keyFragment;

        ConfigKey(final String keyFragment)
        {
            this.keyFragment = keyFragment;
        }

        public String getKey()
        {
            return COMMON_PREFIX + SEPARATOR + keyFragment;
        }
    }
}
