package com.quedex.marketmaker;

import com.google.common.collect.ImmutableMap;
import com.quedex.qdxapi.entities.BuySellBook;
import com.quedex.qdxapi.entities.InstrumentData;
import com.quedex.qdxapi.entities.TradeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class MarketDataManager implements InstrumentDataUpdateable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataManager.class);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final Map<String, TradeInfo> lastTrades = new HashMap<>();
    private Map<String, BuySellBook> orderBook = ImmutableMap.of();

    @Override
    public void update(InstrumentData instrumentData) {
        instrumentData.getTrades().forEach((symbol, trades) -> {
            lastTrades.put(symbol, getLast(trades));
        });
        orderBook = instrumentData.getOrderBook();
        LOGGER.debug("Updated");
    }

    public BigDecimal getLastTradePrice(String symbol) {
        checkArgument(lastTrades.containsKey(symbol), "No last trade for %s", symbol);
        return lastTrades.get(symbol).getPrice();
    }

    public BigDecimal getMid(String symbol) {
        BuySellBook book = orderBook.get(symbol);
        checkArgument(book != null, "No order book for %s", symbol);

        if (!book.getBuyLimits().isEmpty() && !book.getSellLimits().isEmpty()) {

            return (book.getBuyLimits().get(0).getPrice().get().add(book.getSellLimits().get(0).getPrice().get()))
                    .divide(TWO, 8, RoundingMode.HALF_EVEN);

        } else if (!book.getSellLimits().isEmpty()) {

            return book.getSellLimits().get(0).getPrice().get();

        } else if (!book.getBuyLimits().isEmpty()) {

            return book.getBuyLimits().get(0).getPrice().get();

        } else {

            return getLastTradePrice(symbol); // use last in case of empty OB
        }
    }

    private static <T> T getLast(List<T> fromList) {
        return fromList.get(fromList.size() - 1);
    }
}
