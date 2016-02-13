package com.quedex.marketmaker;

import com.quedex.qdxapi.entities.AccountState;
import com.quedex.qdxapi.entities.Instrument;
import com.quedex.qdxapi.entities.OpenPositionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RiskManager implements AccountStateUpdateable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskManager.class);

    private final InstrumentManager instrumentManager;
    private final Map<String, OpenPositionInfo> openPositions = new HashMap<>();

    public RiskManager(InstrumentManager instrumentManager) {
        this.instrumentManager = instrumentManager;
    }

    @Override
    public void update(AccountState accountState) {
        openPositions.clear();
        openPositions.putAll(accountState.getOpenPositions());
        LOGGER.debug("Updated");
    }

    /**
     * @return total delta of traded instruments
     */
    public double getTotalDelta() {
        double sum = 0;
        for (final Instrument futures : instrumentManager.getTradedFutures()) { // currently futures only
            // futures delta = 1
            sum += getOpenPositionQtySigned(futures.getSymbol());
        }
        LOGGER.debug("Total delta on futures: {}", sum);
        return sum;
    }

    private int getOpenPositionQtySigned(String symbol) {
        OpenPositionInfo position = openPositions.get(symbol);
        return position != null ? position.getPositionQuantitySigned() : 0;
    }
}
