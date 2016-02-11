package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpointProviderConfigFactory;
import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpoint;
import com.quedex.marketmaker.qdxapi.endpoint.QdxEndpointProvider;

public class Main {

    public static void main(String... args) {

        QdxEndpoint qdxEndPoint = new QdxEndpointProvider(
                new QdxEndpointProviderConfigFactory("quedex-config-example.properties").getConfiguration()
        ).getQdxEndPoint();

        MarketMaker mm = new MarketMaker(qdxEndPoint);

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
