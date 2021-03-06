package net.quedex.marketmaker;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastFairPriceProvider implements FairPriceProvider
{
    private final MarketDataManager marketDataManager;

    public LastFairPriceProvider(final MarketDataManager marketDataManager)
    {
        this.marketDataManager = checkNotNull(marketDataManager, "null marketDataManager");
    }

    @Override
    public BigDecimal getFairPrice(final int instrumentId)
    {
        return marketDataManager.getLastTradePrice(instrumentId);
    }
}
