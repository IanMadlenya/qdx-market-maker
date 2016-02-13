package com.quedex.marketmaker;

import java.math.BigDecimal;

public class LastFairPriceProvider implements FairPriceProvider {

    private final MarketDataManager marketDataManager;

    public LastFairPriceProvider(MarketDataManager marketDataManager) {
        this.marketDataManager = marketDataManager;
    }

    @Override
    public BigDecimal getFairPrice(String symbol) {
        return marketDataManager.getLastTradePrice(symbol);
    }
}
