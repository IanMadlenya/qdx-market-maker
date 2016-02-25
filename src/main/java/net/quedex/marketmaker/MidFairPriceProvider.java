package net.quedex.marketmaker;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

public class MidFairPriceProvider implements FairPriceProvider {

    private final MarketDataManager marketDataManager;

    public MidFairPriceProvider(MarketDataManager marketDataManager) {
        this.marketDataManager = checkNotNull(marketDataManager, "null marketDataManager");
    }

    @Override
    public BigDecimal getFairPrice(String symbol) {
        return marketDataManager.getMid(symbol);
    }
}
