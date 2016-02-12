package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.Instrument;
import com.quedex.marketmaker.qdxapi.entities.OrderSide;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class UniformFuturesOrderPlacingStrategy implements OrderPlacingStrategy {

    private final FairPriceProvider fairPriceProvider;
    private final RiskManager riskManager;

    private final int levels;
    private final int qtyOnLevel;
    private final double deltaLimit;
    private final BigDecimal spreadFraction;

    public UniformFuturesOrderPlacingStrategy(
            FairPriceProvider fairPriceProvider,
            RiskManager riskManager,
            int levels,
            int qtyOnLevel,
            double deltaLimit,
            BigDecimal spreadFraction
    ) {
        this.fairPriceProvider = fairPriceProvider;
        this.riskManager = riskManager;
        this.levels = levels;
        this.qtyOnLevel = qtyOnLevel;
        this.deltaLimit = deltaLimit;
        this.spreadFraction = spreadFraction;
    }

    @Override
    public Collection<GenericOrder> getOrders(Instrument futures) {
        checkArgument(futures.isFutures(), "Expected futures");

        BigDecimal fairPrice = fairPriceProvider.getFairPrice(futures.getSymbol());
        BigDecimal spread = fairPrice.multiply(spreadFraction);

        List<GenericOrder> orders = new ArrayList<>(levels * 2);
        double totalDelta = riskManager.getTotalDelta();

        if (totalDelta < deltaLimit) {
            orders.addAll(getOrders(futures, OrderSide.BUY, fairPrice, spread.negate()));
        } // otherwise above limit - don't want to increase delta
        if (totalDelta > -deltaLimit) {
            orders.addAll(getOrders(futures, OrderSide.SELL, fairPrice, spread));
        } // otherwise below limit - don't want to decrease delta

        return orders;
    }

    private Collection<GenericOrder> getOrders(
            Instrument futures,
            OrderSide side,
            BigDecimal fairPrice,
            BigDecimal spread
    ) {
        List<GenericOrder> orders = new ArrayList<>(levels);

        for (int i = 1; i <= levels; i++) {

            BigDecimal priceRounded = futures.roundPriceToTickSide(
                    fairPrice.add(spread.multiply(BigDecimal.valueOf(i)))
            );

            orders.add(new GenericOrder(
                    futures.getSymbol(),
                    side,
                    priceRounded,
                    qtyOnLevel
            ));
        }

        return orders;
    }
}
