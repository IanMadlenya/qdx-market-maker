package net.quedex.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class PendingOrders implements Iterable<UserOrderInfo> {

    public static final PendingOrders EMPTY = new PendingOrders(ImmutableList.of(), BigDecimal.ZERO);

    private final ImmutableList<UserOrderInfo> orders;
    private final BigDecimal margin;

    @JsonCreator
    public PendingOrders(
            @JsonProperty("orders") List<UserOrderInfo> orders,
            @JsonProperty("margin") BigDecimal margin
    ) {
        checkArgument(margin.compareTo(BigDecimal.ZERO) >= 0);

        this.orders = ImmutableList.copyOf(orders);
        this.margin = margin;
    }

    public ImmutableList<UserOrderInfo> getOrders() {
        return orders;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    @Override
    public Iterator<UserOrderInfo> iterator() {
        return orders.iterator();
    }

    public Stream<UserOrderInfo> stream() {
        return orders.stream();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orders", orders)
                .add("margin", margin)
                .toString();
    }
}
