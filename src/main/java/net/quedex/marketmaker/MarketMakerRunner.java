package net.quedex.marketmaker;

import net.quedex.api.common.CommunicationException;
import net.quedex.api.market.Instrument;
import net.quedex.api.market.MarketData;
import net.quedex.api.market.MarketStream;
import net.quedex.api.user.AccountState;
import net.quedex.api.user.OrderSpec;
import net.quedex.api.user.UserStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class MarketMakerRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketMakerRunner.class);

    private final MarketData marketData;
    private final MarketStream marketStream;
    private final UserStream userStream;
    private final MarketMakerConfiguration marketMakerConfiguration;
    private final int sleepTimeSeconds;

    private volatile boolean running = false;

    public MarketMakerRunner(
        final MarketData marketData,
        final MarketStream marketStream,
        final UserStream userStream,
        final MarketMakerConfiguration mmConfig)
    {
        this.marketData = checkNotNull(marketData, "null marketData");
        this.marketStream = checkNotNull(marketStream, "null marketStream");
        this.userStream = checkNotNull(userStream, "null userStream");
        this.marketMakerConfiguration = checkNotNull(mmConfig, "null marketMakerConfiguration");
        this.sleepTimeSeconds = mmConfig.getTimeSleepSeconds();
    }

    public void runLoop()
    {
        marketStream.registerStreamFailureListener(this::onError);
        userStream.registerStreamFailureListener(this::onError);

        try
        {
            marketStream.start();
            userStream.start();
        }
        catch (final CommunicationException e)
        {
            LOGGER.error("Error starting streams", e);
            return;
        }

        final MarketMaker marketMaker;
        final Map<Integer, Instrument> instruments;

        try
        {
            instruments = marketData.getInstruments();
            marketMaker = new MarketMaker(
                new RealTimeProvider(),
                marketMakerConfiguration,
                instruments,
                this::onError
            );
        }
        catch (final CommunicationException e)
        {
            LOGGER.error("Error initialising instruments", e);
            return;
        }

        try
        {
            LOGGER.info("Initialising");

            final CompletableFuture<AccountState> initialAccountStateFuture = new CompletableFuture<>();

            marketStream.registerQuotesListener(marketMaker).subscribe(instruments.keySet());

            userStream.registerOpenPositionListener(marketMaker);
            userStream.registerOrderListener(marketMaker);
            userStream.registerAccountStateListener(initialAccountStateFuture::complete);
            userStream.subscribeListeners();

            initialAccountStateFuture.get(); // await initial state
            userStream.registerAccountStateListener(null); // not used anymore

            LOGGER.info("Running");
            running = true;

            while (running)
            {
                final Future<List<OrderSpec>> orderSpecs = marketMaker.recalculate();
                send(orderSpecs.get());

                try
                {
                    Thread.sleep(sleepTimeSeconds * 1000L);
                }
                catch (final InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Terminal error", e);
        }
        finally
        {
            LOGGER.info("Stopping");
            try
            {
                LOGGER.info("Cancelling all pending orders");
                try
                {
                    send(marketMaker.getAllOrderCancels().get());
                    Thread.sleep(10_000);
                }
                catch (final InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch (final ExecutionException e)
                {
                    LOGGER.error("Error getting all order cancels", e);
                }
            }
            finally
            {
                try
                {
                    userStream.stop();
                    marketStream.stop();
                }
                catch (final CommunicationException e)
                {
                    LOGGER.error("Error stopping streams", e);
                }
                LOGGER.info("Stopped");
            }
        }

        marketMaker.stop();

        try
        {
            userStream.stop();
            marketStream.stop();
        }
        catch (final CommunicationException e)
        {
            LOGGER.error("Error stopping streams", e);
        }
    }

    public void stop()
    {
        running = false;
    }

    private void onError(final Exception e)
    {
        LOGGER.error("Async terminal error", e);
        stop();
    }

    private void send(final List<OrderSpec> orderSpecs)
    {
        LOGGER.debug("send({})", orderSpecs);
        if (orderSpecs.isEmpty())
        {
            return;
        }
        userStream.batch(orderSpecs);
    }
}
