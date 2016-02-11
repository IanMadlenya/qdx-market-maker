package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkArgument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class PendingOrders {

    private final ImmutableList<UserOrderInfo> orders;
    private final BigDecimal margin;

    @JsonCreator
    public PendingOrders(
            @JsonProperty("orders") ArrayList<UserOrderInfo> orders,
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orders", orders)
                .add("margin", margin)
                .toString();
    }
}
