package net.quedex.marketmaker;

import net.quedex.api.entities.AccountState;
import net.quedex.api.entities.Instrument;
import net.quedex.api.entities.OpenPositionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class RiskManager implements AccountStateUpdateable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskManager.class);

    private final InstrumentManager instrumentManager;
    private final FairPriceProvider fairVolatilityProvider;
    private final FairPriceProvider futuresFairPriceProvider;
    private final Pricing pricing;
    private final Map<String, OpenPositionInfo> openPositions = new HashMap<>();

    private double totalDelta = 0;
    private double totalVega = 0;
    private double totalGammaP = 0;
    private double totalTheta = 0;

    public RiskManager(
            InstrumentManager instrumentManager,
            FairPriceProvider fairVolatilityProvider,
            FairPriceProvider futuresFairPriceProvider,
            Pricing pricing
    ) {
        this.instrumentManager = checkNotNull(instrumentManager, "null instrumentManager");
        this.fairVolatilityProvider = checkNotNull(fairVolatilityProvider, "null fairVolatilityProvider");
        this.futuresFairPriceProvider = checkNotNull(futuresFairPriceProvider, "null futuresFairPriceProvider");
        this.pricing = checkNotNull(pricing, "null pricing");
    }

    @Override
    public void update(AccountState accountState) {
        openPositions.clear();
        openPositions.putAll(accountState.getOpenPositions());
        updateGreeks();
        LOGGER.debug("Updated");
    }

    private void updateGreeks() {
        totalDelta = 0;
        totalVega = 0;
        totalGammaP = 0;
        totalTheta = 0;
        for (final Map.Entry<String, OpenPositionInfo> openPosition : openPositions.entrySet()) {

            int openPositionSigned = openPosition.getValue().getPositionQuantitySigned();
            Instrument positionInstrument = instrumentManager.getInstrument(openPosition.getKey());

            double futuresPrice = futuresFairPriceProvider.getFairPrice(
                    instrumentManager.getFuturesAtExpiration(positionInstrument.getExpirationDate()).getSymbol()
            ).doubleValue();

            Pricing.Metrics metrics = pricing.calculateMetrics(
                    positionInstrument,
                    fairVolatilityProvider.getFairPrice(positionInstrument.getSymbol()).doubleValue(),
                    futuresPrice
            );

            double positionDelta = openPositionSigned * metrics.getDelta(); // per contract
            double positionGammaP = openPositionSigned * metrics.getGammaP(); // per contract
            double positionTheta = openPositionSigned * metrics.getTheta() * positionInstrument.getNotionalAmount(); // per notional
            double positionVega = openPositionSigned * metrics.getVega() * positionInstrument.getNotionalAmount(); // per notional
            LOGGER.debug("Position {}: {}, delta={}, vega={}, gammaP={}, theta={}",
                    openPosition.getKey(), openPositionSigned, positionDelta, positionVega, positionGammaP, positionTheta);

            totalDelta += positionDelta;
            totalVega += positionVega;
            totalGammaP += positionGammaP;
            totalTheta += positionTheta;
        }
        LOGGER.info("Total: delta={}, vega={}, gammaP={}, theta={}", totalDelta, totalVega, totalGammaP, totalTheta);
    }

    /**
     * @return total delta of traded instruments
     */
    public double getTotalDelta() {
        return totalDelta;
    }

    /**
     * @return total vega of traded instrumetns
     */
    public double getTotalVega() {
        return totalVega;
    }
}
