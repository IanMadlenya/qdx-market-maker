package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpoint;
import com.quedex.marketmaker.qdxapi.entities.AccountState;
import com.quedex.marketmaker.qdxapi.entities.InstrumentData;
import com.quedex.marketmaker.qdxapi.entities.LimitOrderSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

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

    public void stop() {
        Thread.currentThread().interrupt();
        LOGGER.info("Stopping");
    }

    public void runLoop() {

        qdxEndpoint.initialize();

        MarketMaker marketMaker = new MarketMaker(
                new RealTimeProvider(),
                marketMakerConfiguration,
                getInstrumentData(),
                getAccountState()
        );

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
    }

    private void cancelOrders(List<Long> idsToBeCancelled) {
        withRetry(() -> qdxEndpoint.cancelOrders(idsToBeCancelled)); // ignore result
    }

    private void placeOrders(List<LimitOrderSpec> ordersToBePlaced) {
        withRetry(() -> qdxEndpoint.placeOrders(ordersToBePlaced)); // TODO: check result
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
                LOGGER.warn("Failure when retrying", e);
                ex = new IllegalStateException("Failed after retries: " + maxRetry, e);
                retry++;
            }
        }
        throw ex;
    }
}
