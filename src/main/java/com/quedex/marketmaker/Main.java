package com.quedex.marketmaker;

import com.google.common.io.Resources;
import com.quedex.qdxapi.QdxEndpoint;
import com.quedex.qdxapi.QdxEndpointProvider;
import com.quedex.qdxapi.QdxEndpointProviderConfigFactory;

public class Main {
    private Main() { throw new AssertionError(); }

    public static void main(String... args) throws Exception {

        if (args.length != 0 && args.length != 2) {
            printUsageAndExit();
        }

        String qdxConfig;
        String mmConfig;

        if (args.length > 0) {
            qdxConfig = args[0];
            mmConfig = args[1];
        } else {
            qdxConfig = Resources.getResource("quedex-config.properties").toString();
            mmConfig = Resources.getResource("market-maker.properties").toString();
        }

        QdxEndpoint qdxEndPoint = new QdxEndpointProvider(
                new QdxEndpointProviderConfigFactory(qdxConfig).getConfiguration()
        ).getQdxEndPoint();

        MarketMakerRunner mm = new MarketMakerRunner(
                qdxEndPoint,
                MarketMakerConfiguration.fromPropertiesFile(mmConfig)
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

    private static void printUsageAndExit() {
        System.out.println("Usage: java -jar <jar name> <Quedex properties filename> <market maker properties file name>");
        System.exit(1);
    }
}
