package com.quedex.marketmaker;

import com.quedex.qdxapi.QdxEndpoint;
import com.quedex.qdxapi.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MarketMakerRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketMakerRunner.class);

    private final QdxEndpoint qdxEndpoint;
    private final MarketMakerConfiguration marketMakerConfiguration;
    private final int maxRetry;
    private final int sleepTimeSeconds;

    public MarketMakerRunner(QdxEndpoint qdxEndpoint, MarketMakerConfiguration marketMakerConfiguration) {
        this.qdxEndpoint = checkNotNull(qdxEndpoint, "null qdxEndpoint");
        this.marketMakerConfiguration = checkNotNull(marketMakerConfiguration, "null marketMakerConfiguration");
        this.maxRetry = marketMakerConfiguration.getMaxConnectionRetry();
        this.sleepTimeSeconds = marketMakerConfiguration.getTimeSleepSeconds();
    }

    public void runLoop() {
        try {
            LOGGER.info("Initialising");
            qdxEndpoint.initialize();

            MarketMaker marketMaker = new MarketMaker(
                    new RealTimeProvider(),
                    marketMakerConfiguration,
                    getInstrumentData(),
                    getAccountState()
            );

            LOGGER.info("Running");
            while (!Thread.currentThread().isInterrupted()) {

                marketMaker.update(getAccountState());
                marketMaker.update(getInstrumentData());
                marketMaker.recalculate();

                cancelOrders(marketMaker.getOrdersToCancel());
                placeOrders(marketMaker.getOrdersToPlace());

                try {
                    Thread.sleep(sleepTimeSeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Terminal error", e);
        } finally {
            LOGGER.info("Stopping");
            try {
                LOGGER.info("Cancelling all pending orders");
                cancelOrders(
                        getAccountState().getPendingOrders().values()
                                .stream()
                                .flatMap(PendingOrders::stream)
                                .map(UserOrderInfo::getClientOrderId)
                                .collect(Collectors.toList())
                );
            } finally {
                qdxEndpoint.stop();
                LOGGER.info("Stopped");
            }
        }
    }

    private void cancelOrders(List<Long> idsToBeCancelled) {
        if (idsToBeCancelled.isEmpty()) {
            LOGGER.debug("Nothing to cancel");
            return;
        }
        LOGGER.debug("Cancelling: {}", idsToBeCancelled);
        withRetry(() -> qdxEndpoint.cancelOrders(idsToBeCancelled)); // ignore result
    }

    private void placeOrders(List<LimitOrderSpec> ordersToBePlaced) {
        if (ordersToBePlaced.isEmpty()) {
            LOGGER.debug("Nothing to place");
            return;
        }
        LOGGER.debug("Placing: {}", ordersToBePlaced);
        List<OrderPlaceResult> results = withRetry(() -> qdxEndpoint.placeOrders(ordersToBePlaced));
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isSuccess()) {
                LOGGER.error("Order: {} failed: {}",
                        ordersToBePlaced.get(i), results.get(i).getErrorMessage().replaceAll("\\n", " "));
            }
        }
    }

    private AccountState getAccountState() {
        return withRetry(qdxEndpoint::getAccountState);
    }

    private InstrumentData getInstrumentData() {
        return withRetry(qdxEndpoint::getInstrumentData);
    }

    private <T> T withRetry(Callable<T> supplier) {
        int retry = 0;
        RuntimeException ex = null;
        while (retry < maxRetry) {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    ex = new IllegalStateException("Failed after retries: " + maxRetry, e);
                    break;
                }
                LOGGER.warn("Failure when retrying", e);
                retry++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw ex;
    }
}
