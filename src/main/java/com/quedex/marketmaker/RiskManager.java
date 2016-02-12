package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.AccountState;
import com.quedex.marketmaker.qdxapi.entities.Instrument;
import com.quedex.marketmaker.qdxapi.entities.OpenPositionInfo;

import java.util.HashMap;
import java.util.Map;

public class RiskManager implements AccountStateUpdateable {

    private final InstrumentManager instrumentManager;
    private final Map<String, OpenPositionInfo> openPositions = new HashMap<>();

    public RiskManager(InstrumentManager instrumentManager) {
        this.instrumentManager = instrumentManager;
    }

    @Override
    public void update(AccountState accountState) {
        openPositions.clear();
        openPositions.putAll(accountState.getOpenPositions());
    }

    /**
     * @return total delta of traded instruments
     */
    public double getTotalDelta() {
        double sum = 0;
        for (final Instrument futures : instrumentManager.getTradedFutures()) { // currently futures only
            OpenPositionInfo position = openPositions.get(futures.getSymbol());
            // futures delta = 1
            sum += position.getPositionQuantitySigned() * futures.getNotionalAmount();
        }
        return sum;
    }
}
