package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpoint;
import com.quedex.marketmaker.qdxapi.entities.LimitOrderSpec;
import com.quedex.marketmaker.qdxapi.entities.OrderPlaceResult;
import com.quedex.marketmaker.qdxapi.entities.OrderSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class MarketMaker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketMaker.class);
    private static final int MAX_RETRY = 3;

    private final QdxEndpoint qdxEndpoint;

    public MarketMaker(QdxEndpoint qdxEndpoint) {
        this.qdxEndpoint = checkNotNull(qdxEndpoint, "null qdxEndpoint");
    }

    public void runLoop() {

        qdxEndpoint.initialize();

        while (!Thread.currentThread().isInterrupted()) {

            System.out.println(withRetry(qdxEndpoint::getInstrumentData).getOrderBook().get("F.USD.MAR16"));
            System.out.println(withRetry(qdxEndpoint::getSpotData));

            OrderPlaceResult orderPlaceResult = withRetry(() -> qdxEndpoint.placeOrder(new LimitOrderSpec(
                1, "F.USD.MAR16", OrderSide.BUY, 5, new BigDecimal("0.0025")
            )));
            withRetry(() -> qdxEndpoint.cancelOrder(1));

            System.out.println(orderPlaceResult);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        Thread.currentThread().interrupt();
        LOGGER.info("Stopping");
    }

    private static <T> T withRetry(Callable<T> supplier) {
        int retry = 0;
        RuntimeException ex = null;
        while (retry < MAX_RETRY) {
            try {
                return supplier.call();
            } catch (Exception e) {
                LOGGER.warn("Failure when retrying", e);
                ex = new IllegalStateException("Failed after retries: " + MAX_RETRY, e);
                retry++;
            }
        }
        throw ex;
    }
}
