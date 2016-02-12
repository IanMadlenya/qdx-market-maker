package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NotThreadSafe
public class MarketMaker implements AccountStateUpdateable, InstrumentDataUpdateable {

    private final AccountStateUpdateable[] accountStateUpdateables;
    private final InstrumentDataUpdateable[] instrumentDataUpdateables;

    private final InstrumentManager instrumentManager;
    private final OrderPlacingStrategy orderPlacingStrategy;

    private final List<Long> ordersToCancel = new ArrayList<>();
    private final List<LimitOrderSpec> ordersToPlace = new ArrayList<>();
    private final List<OrderModificationSpec> ordersToModify = new ArrayList<>();
    private long orderIdCounter = 0;

    private AccountState accountState;

    public MarketMaker(
            TimeProvider timeProvider,
            MarketMakerConfiguration config,
            InstrumentData instrumentData,
            AccountState accountState
    ) {
        instrumentManager = new InstrumentManager(timeProvider, instrumentData.getInstrumentInfo());
        RiskManager riskManager = new RiskManager(instrumentManager);
        MarketDataManager marketDataManager = new MarketDataManager();
        orderPlacingStrategy = new UniformFuturesOrderPlacingStrategy(
                marketDataManager,
                riskManager,
                config.getNumLevels(),
                config.getQtyOnLevel(),
                config.getDeltaLimit(),
                config.getSpreadFraction()
        );

        accountStateUpdateables = new AccountStateUpdateable[]{ riskManager };
        instrumentDataUpdateables = new InstrumentDataUpdateable[]{ marketDataManager };

        orderIdCounter = accountState.getPendingOrders().values()
                .stream()
                .flatMap(PendingOrders::stream)
                .mapToLong(UserOrderInfo::getClientOrderId)
                .max()
                .orElse(0);

        update(instrumentData);
        update(accountState);
    }

    @Override
    public void update(AccountState accountState) {
        for (final AccountStateUpdateable accountStateUpdateable : accountStateUpdateables) {
            accountStateUpdateable.update(accountState);
        }
        this.accountState = accountState;
    }

    @Override
    public void update(InstrumentData instrumentData) {
        for (final InstrumentDataUpdateable instrumentDataUpdateable : instrumentDataUpdateables) {
            instrumentDataUpdateable.update(instrumentData);
        }
    }

    public void recalculate() {

        ordersToPlace.clear();

        for (final Instrument futures : instrumentManager.getTradedFutures()) {

            ordersToPlace.addAll(
                    orderPlacingStrategy.getOrders(futures).stream()
                            .map(o -> o.toLimitOrderSpec(++orderIdCounter))
                            .collect(Collectors.toList())
            );
        }

        ordersToCancel.clear();
        ordersToCancel.addAll(
                accountState.getPendingOrders().values()
                        .stream()
                        .flatMap(PendingOrders::stream)
                        .map(UserOrderInfo::getClientOrderId)
                        .collect(Collectors.toList())
        );
    }

    public List<Long> getOrdersToCancel() {
        return ordersToCancel;
    }

    public List<LimitOrderSpec> getOrdersToPlace() {
        return ordersToPlace;
    }

    public List<OrderModificationSpec> getOrdersToModify() {
        return ordersToModify;
    }
}
