package net.quedex.marketmaker;

import net.quedex.api.market.Instrument;
import net.quedex.api.market.Quotes;
import net.quedex.api.market.QuotesListener;
import net.quedex.api.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
public class MarketMaker implements QuotesListener, OrderListener, OpenPositionListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketMaker.class);

    private final QuotesListener[] quotesListeners;
    private final OrderListener[] orderListeners;
    private final OpenPositionListener[] openPositionListeners;

    /**
     * Thread confinement to this thread guarantees thread-safety of the whole application.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Consumer<Exception> exceptionHandler;

    private final InstrumentManager instrumentManager;
    private final OrderPlacingStrategy futuresOrderPalcingStrategy;
    private final OrderPlacingStrategy optionOrderPlacingStrategy;
    private final FairPriceProvider futuresFairPriceProvider;
    private final OrderManager orderManager;

    private final Map<Integer, BigDecimal> previousFairPrice = new HashMap<>();

    private final int numLevels;
    private final int qtyOnLevel;

    public MarketMaker(
        final TimeProvider timeProvider,
        final MarketMakerConfiguration config,
        final Map<Integer, Instrument> instrumentData,
        final Consumer<Exception> exceptionHandler)
    {
        instrumentManager = new InstrumentManager(timeProvider, instrumentData);
        final MarketDataManager marketDataManager = new MarketDataManager();
        futuresFairPriceProvider = new LastFairPriceProvider(marketDataManager);
        final FairPriceProvider fairVolatilityProvider = s -> BigDecimal.valueOf(config.getFairVolatility());
        final Pricing pricing = new Pricing(
            timeProvider,
            config.getSabrBeta(),
            config.getSabrVolOfVol(),
            config.getSabrRho(),
            config.useSabrTimeAdjustedVolOfVol()
        );
        final RiskManager riskManager = new RiskManager(
            instrumentManager,
            fairVolatilityProvider,
            futuresFairPriceProvider,
            pricing
        );
        futuresOrderPalcingStrategy = new UniformFuturesOrderPlacingStrategy(
            futuresFairPriceProvider,
            riskManager,
            config.getNumLevels(),
            config.getQtyOnLevel(),
            config.getDeltaLimit(),
            config.getFuturesSpreadFraction()
        );
        optionOrderPlacingStrategy = new UniformOptionOrderPlacingStrategy(
            fairVolatilityProvider,
            futuresFairPriceProvider,
            riskManager,
            instrumentManager,
            pricing,
            config.getNumLevels(),
            config.getQtyOnLevel(),
            config.getDeltaLimit(),
            config.getVegaLimit(),
            config.getVolatilitySpreadFraction()
        );
        orderManager = new OrderManager();

        quotesListeners = new QuotesListener[] {marketDataManager};
        orderListeners = new OrderListener[] {orderManager};
        openPositionListeners = new OpenPositionListener[] {riskManager};

        numLevels = config.getNumLevels();
        qtyOnLevel = config.getQtyOnLevel();
        this.exceptionHandler = checkNotNull(exceptionHandler, "null exceptionHandler");
    }

    public Future<List<OrderSpec>> recalculate()
    {
        return executor.submit(this::recalculateNoSync);
    }

    public Future<List<OrderSpec>> getAllOrderCancels()
    {
        return executor.submit(
            () -> orderManager.getAllOrderIds().stream().map(OrderCancelSpec::new).collect(Collectors.toList())
        );
    }

    public void stop()
    {
        executor.shutdown();
    }

    private List<OrderSpec> recalculateNoSync()
    {
        // TODO: sensitivity to fair price

        try
        {
            final List<OrderSpec> orderSpecs = new ArrayList<>();

            for (final Instrument futures : instrumentManager.getTradedFutures())
            {
                final int instrId = futures.getInstrumentId();
                final BigDecimal fairPrice = futuresFairPriceProvider.getFairPrice(instrId);

                if (!previousFairPrice.containsKey(instrId) || previousFairPrice.get(instrId).compareTo(fairPrice) != 0)
                {
                    orderSpecs.addAll(
                        orderManager.getOrderIdsForInstrument(instrId)
                            .stream()
                            .map(OrderCancelSpec::new)
                            .collect(Collectors.toList())
                    );

                    orderSpecs.addAll(
                        futuresOrderPalcingStrategy.getOrders(futures).stream()
                            .map(o -> o.toLimitOrderSpec(orderManager.getNextOrderId()))
                            .collect(Collectors.toList())
                    );

                    previousFairPrice.put(instrId, fairPrice);
                }
            }

            for (final Instrument option : instrumentManager.getTradedOptions())
            {
                // TODO: replace orders also on fair price change

                final int instrId = option.getInstrumentId();
                final int sumPlacedOrderQty = orderManager.getSumPlacedQtyForInstrument(instrId);

                if (sumPlacedOrderQty < numLevels * qtyOnLevel * 2) // any filled
                {
                    orderSpecs.addAll(
                        orderManager.getOrderIdsForInstrument(instrId)
                            .stream()
                            .map(OrderCancelSpec::new)
                            .collect(Collectors.toList())
                    );

                    orderSpecs.addAll(
                        optionOrderPlacingStrategy.getOrders(option).stream()
                            .map(o -> o.toLimitOrderSpec(orderManager.getNextOrderId()))
                            .collect(Collectors.toList())
                    );
                }
            }
            return orderSpecs;
        }
        catch (final RuntimeException e)
        {
            exceptionHandler.accept(e);
            throw e;
        }
    }

    @Override
    public void onQuotes(final Quotes quotes)
    {
        catchingExecute(() ->
        {
            for (final QuotesListener quotesListener : quotesListeners)
            {
                quotesListener.onQuotes(quotes);
            }
        });
    }

    @Override
    public void onOpenPosition(final OpenPosition openPosition)
    {
        catchingExecute(() ->
        {
            for (final OpenPositionListener openPositionListener : openPositionListeners)
            {
                openPositionListener.onOpenPosition(openPosition);
            }
        });
    }

    @Override
    public void onOrderPlaced(final OrderPlaced orderPlaced)
    {
        catchingExecute(() ->
        {
            for (final OrderListener orderListener : orderListeners)
            {
                orderListener.onOrderPlaced(orderPlaced);
            }
        });
    }

    @Override
    public void onOrderPlaceFailed(final OrderPlaceFailed orderPlaceFailed)
    {
        LOGGER.error("{}", orderPlaceFailed);
    }

    @Override
    public void onOrderCanceled(final OrderCanceled orderCanceled)
    {
        catchingExecute(() ->
        {
            for (final OrderListener orderListener : orderListeners)
            {
                orderListener.onOrderCanceled(orderCanceled);
            }
        });
    }

    @Override
    public void onOrderCancelFailed(final OrderCancelFailed orderCancelFailed)
    {
        LOGGER.error("{}", orderCancelFailed);
    }

    @Override
    public void onOrderModified(final OrderModified orderModified)
    {
        // no-op
    }

    @Override
    public void onOrderModificationFailed(final OrderModificationFailed orderModificationFailed)
    {
        // no-op
    }

    @Override
    public void onOrderFilled(final OrderFilled orderFilled)
    {
        catchingExecute(() ->
        {
            for (final OrderListener orderListener : orderListeners)
            {
                orderListener.onOrderFilled(orderFilled);
            }
        });
    }

    private void catchingExecute(final Runnable runnable)
    {
        executor.execute(() ->
        {
            try
            {
                runnable.run();
            }
            catch (final RuntimeException e)
            {
                exceptionHandler.accept(e);
                throw e;
            }
        });
    }
}
