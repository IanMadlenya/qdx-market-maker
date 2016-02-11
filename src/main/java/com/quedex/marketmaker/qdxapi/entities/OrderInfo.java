package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public class OrderInfo {

    private final long systemOrderId;
    private final Optional<BigDecimal> price;
    private final int quantity;
    private final int initialQuantity;

    public OrderInfo(
            @JsonProperty("system_order_id") long systemOrderId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("initial_quantity") int initialQuantity
    ) {
        this.systemOrderId = systemOrderId;
        this.price = Optional.ofNullable(price);
        this.quantity = quantity;
        this.initialQuantity = initialQuantity;
    }

    public long getSystemOrderId() {
        return systemOrderId;
    }

    public Optional<BigDecimal> getPrice() {
        return price;
    }

    public int getQuantityLeft() {
        return quantity;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public int getFilledQuantity() {
        return initialQuantity - quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderInfo that = (OrderInfo) o;

        return this.systemOrderId == that.systemOrderId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(systemOrderId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("systemOrderId", systemOrderId)
                .add("price", price)
                .add("quantity", quantity)
                .add("initialQuantity", initialQuantity)
                .toString();
    }
}
