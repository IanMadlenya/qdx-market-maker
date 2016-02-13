package com.quedex.marketmaker;

import java.math.BigDecimal;

public class MidFairPriceProvider implements FairPriceProvider {

    private final MarketDataManager marketDataManager;

    public MidFairPriceProvider(MarketDataManager marketDataManager) {
        this.marketDataManager = marketDataManager;
    }

    @Override
    public BigDecimal getFairPrice(String symbol) {
        return marketDataManager.getMid(symbol);
    }
}
