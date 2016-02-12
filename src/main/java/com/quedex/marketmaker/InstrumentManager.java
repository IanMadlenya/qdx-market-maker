package com.quedex.marketmaker;

import com.quedex.qdxapi.entities.Instrument;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstrumentManager {

    private final TimeProvider timeProvider;
    private final Map<String, Instrument> instruments;

    public InstrumentManager(TimeProvider timeProvider, Map<String, Instrument> instruments) {
        this.timeProvider = timeProvider;
        this.instruments = instruments;
    }

    public List<Instrument> getTradedFutures() {
        return instruments.values().stream()
                .filter(Instrument::isFutures)
                .filter(i -> i.isTraded(timeProvider.getCurrentTime()))
                .collect(Collectors.toList());
    }
}
