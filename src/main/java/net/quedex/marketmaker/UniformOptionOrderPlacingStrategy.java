package net.quedex.marketmaker;

import net.quedex.api.entities.Instrument;
import net.quedex.api.entities.InstrumentType;
import net.quedex.api.entities.OrderSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class UniformOptionOrderPlacingStrategy implements OrderPlacingStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniformOptionOrderPlacingStrategy.class);

    private final FairPriceProvider fairVolatilityProvider;
    private final FairPriceProvider futuresFairPriceProvider;
    private final RiskManager riskManager;
    private final InstrumentManager instrumentManager;
    private final Pricing pricing;

    private final int levels;
    private final int qtyOnLevel;
    private final double deltaLimit;
    private final double vegaLimit;
    private final double volaSpreadFraction;

    public UniformOptionOrderPlacingStrategy(
            FairPriceProvider fairVolatilityProvider,
            FairPriceProvider futuresFairPriceProvider,
            RiskManager riskManager,
            InstrumentManager instrumentManager,
            Pricing pricing,
            int levels,
            int qtyOnLevel,
            double deltaLimit,
            double vegaLimit,
            double volaSpreadFraction
    ) {
        checkArgument(levels >= 0, "numLevels=%s < 0", levels);
        checkArgument(qtyOnLevel > 0, "qtyOnLevel=%s <= 0", qtyOnLevel);
        checkArgument(deltaLimit >= 0, "deltaLimit=%s < 0", deltaLimit);
        checkArgument(vegaLimit >= 0, "vegaLimit=%s < 0", vegaLimit);
        checkArgument(volaSpreadFraction > 0, "volaSpreadFraction=%s <= 0", volaSpreadFraction);
        this.fairVolatilityProvider =  checkNotNull(fairVolatilityProvider, "null fairVolatilityProvider");
        this.futuresFairPriceProvider = checkNotNull(futuresFairPriceProvider, "null futuresFairPriceProvider");
        this.riskManager = checkNotNull(riskManager, "null riskManager");
        this.instrumentManager = checkNotNull(instrumentManager, "null instrumentManager");
        this.pricing = checkNotNull(pricing, "null pricing");
        this.levels = levels;
        this.qtyOnLevel = qtyOnLevel;
        this.deltaLimit = deltaLimit;
        this.vegaLimit = vegaLimit;
        this.volaSpreadFraction = volaSpreadFraction;
    }

    @Override
    public Collection<GenericOrder> getOrders(Instrument option) {
        checkArgument(!option.isFutures(), "Expected option");

        double fairVola = fairVolatilityProvider.getFairPrice(option.getSymbol()).doubleValue();
        double volaSpread = volaSpreadFraction * fairVola;
        double fairFuturesPrice = futuresFairPriceProvider.getFairPrice(
                instrumentManager.getFuturesAtExpiration(option.getExpirationDate()).getSymbol()
        ).doubleValue();

        List<GenericOrder> orders = new ArrayList<>(levels * 2);
        double totalDelta = riskManager.getTotalDelta();
        double totalVega = riskManager.getTotalVega();

        boolean placeBuys = true;
        boolean placeSells = true;

        if (option.getInstrumentType() == InstrumentType.OPTION_EUROPEAN_CALL) {

            if (totalDelta >= deltaLimit) {
                placeBuys = false;
            } else if (totalDelta <= -deltaLimit) {
                placeSells = false;
            }

        } else {

            if (totalDelta >= deltaLimit) {
                placeSells = false;
            } else if (totalDelta <= -deltaLimit) {
                placeBuys = false;
            }
        }

        if (totalVega >= vegaLimit) {
            placeBuys = false;
        } else if (totalVega <= -vegaLimit) {
            placeSells = false;
        }

        BigDecimal bid = null;
        BigDecimal ask = null;

        if (placeBuys) {
            bid = addOrders(orders, option, OrderSide.BUY, fairVola, -volaSpread, fairFuturesPrice);
        }
        if (placeSells) {
            ask = addOrders(orders, option, OrderSide.SELL, fairVola, volaSpread, fairFuturesPrice);
        }

        LOGGER.info("Generated orders {}: Bid = {}, Ask = {}", option.getSymbol(), bid, ask);

        if (bid != null && ask != null) {
            checkState(bid.compareTo(ask) < 0, "bid=%s >= %s=ask", bid, ask);
        }

        return orders;
    }

    private BigDecimal addOrders(
            List<GenericOrder> orders,
            Instrument option,
            OrderSide side,
            double fairVola,
            double spread,
            double futuresPrice
    ) {
        BigDecimal best = null;

        for (int i = 1; i <= levels; i++) {

            BigDecimal priceRounded = option.roundPriceToTickSize(
                    BigDecimal.valueOf(pricing.calculateMetrics(option, fairVola + i * spread, futuresPrice).getPrice()),
                    side == OrderSide.BUY ? RoundingMode.DOWN : RoundingMode.UP
            );

            if (priceRounded.compareTo(BigDecimal.ZERO) == 0) {
                if (side == OrderSide.BUY) {
                    continue;
                } else {
                    priceRounded = option.getTickSize();
                }
            }

            if (best == null) {
                best = priceRounded;
            }

            orders.add(new GenericOrder(
                    option.getSymbol(),
                    side,
                    priceRounded,
                    qtyOnLevel
            ));
        }

        return best;
    }
}
