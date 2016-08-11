package net.quedex.marketmaker;

import com.google.common.collect.ImmutableMap;
import net.quedex.api.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class OrderManager implements OrderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderManager.class);

    private final Map<Integer, Map<Long, GenericOrder>> instrumentIdToOrderIdToOrder = new HashMap<>();
    private final Map<Long, GenericOrder> orderIdToOrder = new HashMap<>();

    private long maxOrderId;

    public Collection<Long> getOrderIdsForInstrument(int instrumentId) {
        return instrumentIdToOrderIdToOrder.getOrDefault(instrumentId, ImmutableMap.of()).keySet();
    }

    public long getNextOrderId() {
        return ++maxOrderId;
    }

    public int getSumPlacedQtyForInstrument(int instrumentId) { // TODO: cache this values
        return instrumentIdToOrderIdToOrder.getOrDefault(instrumentId, ImmutableMap.of()).values()
                .stream()
                .mapToInt(GenericOrder::getQuantity)
                .sum();
    }

    public Collection<Long> getAllOrderIds() {
        return orderIdToOrder.keySet();
    }

    @Override
    public void onOrderPlaced(OrderPlaced orderPlaced) {
        LOGGER.debug("{}", orderPlaced);

        int instrumentId = orderPlaced.getInstrumentId();
        Map<Long, GenericOrder> orderIdToOrder = instrumentIdToOrderIdToOrder.get(instrumentId);
        if (orderIdToOrder == null) {
            orderIdToOrder = new HashMap<>();
            instrumentIdToOrderIdToOrder.put(instrumentId, orderIdToOrder);
        }
        long clientOrderId = orderPlaced.getClientOrderId();
        GenericOrder genericOrder = new GenericOrder(orderPlaced);
        orderIdToOrder.put(clientOrderId, genericOrder);
        this.orderIdToOrder.put(clientOrderId, genericOrder);

        maxOrderId = Math.max(maxOrderId, orderPlaced.getClientOrderId());
    }

    @Override
    public void onOrderCanceled(OrderCanceled orderCanceled) {
        LOGGER.debug("{}", orderCanceled);

        removeOrder(orderCanceled.getClientOrderId());
    }

    @Override
    public void onOrderFilled(OrderFilled orderFilled) {
        long clientOrderId = orderFilled.getClientOrderId();
        GenericOrder genericOrder = orderIdToOrder.get(clientOrderId);
        checkState(genericOrder != null, "Filled order id=%s not found", clientOrderId);
        genericOrder.fill(orderFilled.getFilledQuantity());
        if (genericOrder.isFullyFilled()) {
            removeOrder(clientOrderId);
        }

        LOGGER.debug("fill={}, orderAfterFill={}", orderFilled, genericOrder);
    }

    private void removeOrder(long clientOrderId) {
        GenericOrder genericOrder = orderIdToOrder.remove(clientOrderId);
        checkState(genericOrder != null, "Removed order id=%s not found", clientOrderId);
        Map<Long, GenericOrder> orderIdToOrder = instrumentIdToOrderIdToOrder.get(genericOrder.getInstrumentId());
        orderIdToOrder.remove(clientOrderId);
    }

    @Override
    public void onOrderPlaceFailed(OrderPlaceFailed orderPlaceFailed) { /* no-op */ }

    @Override
    public void onOrderCancelFailed(OrderCancelFailed orderCancelFailed) { /* no-op */ }

    @Override
    public void onOrderModified(OrderModified orderModified) { /* no-op */ }

    @Override
    public void onOrderModificationFailed(OrderModificationFailed orderModificationFailed) { /* no-op */ }
}
