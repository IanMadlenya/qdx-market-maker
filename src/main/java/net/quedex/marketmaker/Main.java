package net.quedex.marketmaker;

import com.google.common.io.Resources;
import net.quedex.api.common.Config;
import net.quedex.api.market.HttpMarketData;
import net.quedex.api.market.MarketData;
import net.quedex.api.market.MarketStream;
import net.quedex.api.market.WebsocketMarketStream;
import net.quedex.api.user.UserStream;
import net.quedex.api.user.WebsocketUserStream;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main
{
    private Main()
    {
        throw new AssertionError();
    }

    public static void main(final String... args) throws Exception
    {
        if (args.length != 0 && args.length != 2)
        {
            printUsageAndExit();
        }

        final InputStream qdxConfigIS;
        final String mmConfigPath;

        if (args.length > 0)
        {
            qdxConfigIS = Files.newInputStream(Paths.get(args[0]));
            mmConfigPath = args[1];
        }
        else
        {
            qdxConfigIS = Resources.getResource("quedex-config.properties").openStream();
            mmConfigPath = Resources.getResource("market-maker.properties").toString();
        }

        final Config qdxConfig = Config.fromInputStream(qdxConfigIS, "qwer".toCharArray());
        final MarketData marketData = new HttpMarketData(qdxConfig);
        final MarketStream marketStream = new WebsocketMarketStream(qdxConfig);
        final UserStream userStream = new WebsocketUserStream(qdxConfig);


        final MarketMakerRunner mm = new MarketMakerRunner(
            marketData,
            marketStream,
            userStream,
            MarketMakerConfiguration.fromPropertiesFile(mmConfigPath)
        );

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                mm.stop();
                try
                {
                    Thread.sleep(10_000);
                }
                catch (final InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        });

        mm.runLoop();
    }

    private static void printUsageAndExit()
    {
        System.out.println("Usage: java -jar <jar name> <Quedex properties filename> <market maker properties file name>");
        System.exit(1);
    }
}
