package net.quedex.marketmaker;

import net.quedex.api.entities.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstrumentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentManager.class);

    private final TimeProvider timeProvider;
    private final Map<String, Instrument> instruments;

    public InstrumentManager(TimeProvider timeProvider, Map<String, Instrument> instruments) {
        this.timeProvider = timeProvider;
        this.instruments = instruments;

        LOGGER.info("Initialised with instruments: {}", instruments);
    }

    public List<Instrument> getTradedFutures() {
        return instruments.values().stream()
                .filter(Instrument::isFutures)
                .filter(i -> i.isTraded(timeProvider.getCurrentTime()))
                .collect(Collectors.toList());
    }
}
