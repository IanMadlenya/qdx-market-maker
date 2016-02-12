package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpoint;
import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpointProvider;
import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpointProviderConfigFactory;

import java.math.BigDecimal;

public class Main {

    public static void main(String... args) {

        QdxEndpoint qdxEndPoint = new QdxEndpointProvider(
                new QdxEndpointProviderConfigFactory("quedex-config-example.properties").getConfiguration()
        ).getQdxEndPoint();

        MarketMakerRunner mm = new MarketMakerRunner(
                qdxEndPoint,
                new MarketMakerConfiguration(
                        3,
                        5,
                        new BigDecimal("0.01"),
                        5,
                        10,
                        100
                )
        );

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mm.stop();
            }
        });
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            mm.stop();
            e.printStackTrace();
        });

        mm.runLoop();
    }
}
