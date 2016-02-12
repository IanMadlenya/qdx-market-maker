package com.quedex.marketmaker;

import com.quedex.qdxapi.entities.InstrumentData;
import com.quedex.qdxapi.entities.TradeInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketDataManager implements FairPriceProvider, InstrumentDataUpdateable {

    private static final int NUM_LAST_PRICES = 2;

    private final Map<String, List<TradeInfo>> recordedLastTrades = new HashMap<>();

    @Override
    public void update(InstrumentData instrumentData) {
        // we want to have last *recorded* trades
        instrumentData.getTrades().forEach((symbol, trades) -> {
            List<TradeInfo> saved = recordedLastTrades.getOrDefault(symbol, new ArrayList<>(NUM_LAST_PRICES));
            if (saved.isEmpty()) {
                saved.add(getLast(trades));
            } else {
                if (!getLast(saved).equals(getLast(trades))) { // new trade arrived
                    if (saved.size() == NUM_LAST_PRICES) {
                        saved.remove(0);
                    }
                    saved.add(getLast(trades));
                }
            }
            recordedLastTrades.put(symbol, saved);
        });
    }

    public BigDecimal getLastTradePrice(String symbol) {
        List<TradeInfo> trades = recordedLastTrades.get(symbol);
        return getLast(trades).getPrice();
    }

    @Override
    public BigDecimal getFairPrice(String symbol) {
        return getLastTradePrice(symbol);
    }

    private static <T> T getLast(List<T> fromList) {
        return fromList.get(fromList.size() - 1);
    }
}
