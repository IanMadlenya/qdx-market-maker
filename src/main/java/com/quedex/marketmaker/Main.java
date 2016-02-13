package com.quedex.marketmaker;

import com.quedex.qdxapi.QdxEndpoint;
import com.quedex.qdxapi.QdxEndpointProvider;
import com.quedex.qdxapi.QdxEndpointProviderConfigFactory;

import java.math.BigDecimal;

public class Main {
    private Main() { throw new AssertionError(); }

    public static void main(String... args) {

        QdxEndpoint qdxEndPoint = new QdxEndpointProvider(
                new QdxEndpointProviderConfigFactory("quedex-config.properties").getConfiguration()
        ).getQdxEndPoint();

        MarketMakerRunner mm = new MarketMakerRunner(
                qdxEndPoint,
                // TODO: move to a config file
                new MarketMakerConfiguration(
                        5,                        // max number of connection retires to QDX
                        10,                       // sleep time between market maker actions in seconds
                        new BigDecimal("0.0015"), // (= 0.15%) spread fraction (on a single side!)
                        new BigDecimal("0.0015"), // (= 0.15%) sensitivity to fair price change TODO: not used yet!
                        5,                        // number of levels orders are placed on (on a single side!)
                        50,                       // quantity of order on a single level
                        250                       // delta limit (number of contracts)
                )
        );

        Thread runnerThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                runnerThread.interrupt();
                try {
                    runnerThread.join(1000L * 30); // wait 30 seconds max
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        mm.runLoop();
    }
}
