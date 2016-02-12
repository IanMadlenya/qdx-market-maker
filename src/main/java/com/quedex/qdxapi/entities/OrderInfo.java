package com.quedex.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public class OrderInfo {

    private final String symbol;
    private final long systemOrderId;
    private final Optional<BigDecimal> price;
    private final OrderSide side;
    private final int quantity;
    private final int initialQuantity;

    public OrderInfo(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("system_order_id") long systemOrderId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("initial_quantity") int initialQuantity
    ) {
        checkArgument(!symbol.isEmpty(), "Empty symbol");
        this.symbol = symbol;
        this.systemOrderId = systemOrderId;
        this.price = Optional.ofNullable(price);
        this.side = checkNotNull(side, "null side");
        this.quantity = quantity;
        this.initialQuantity = initialQuantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getSystemOrderId() {
        return systemOrderId;
    }

    public Optional<BigDecimal> getPrice() {
        return price;
    }

    public OrderSide getSide() {
        return side;
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
                .add("symbol", symbol)
                .add("systemOrderId", systemOrderId)
                .add("price", price)
                .add("side", side)
                .add("quantity", quantity)
                .add("initialQuantity", initialQuantity)
                .toString();
    }
}
