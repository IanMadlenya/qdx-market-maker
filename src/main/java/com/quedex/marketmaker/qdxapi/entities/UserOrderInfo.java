package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class UserOrderInfo extends OrderInfo {

    private final long userOrderId;
    private final long timestamp;
    private final OrderSide side;

    @JsonCreator
    public UserOrderInfo(
            @JsonProperty("system_order_id") long systemOrderId,
            @JsonProperty("order_id") long userOrderId,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("initial_quantity") int initialQuantity,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("price") BigDecimal price
    ) {
        super(systemOrderId, price, quantity, initialQuantity);
        this.userOrderId = userOrderId;
        this.timestamp = timestamp;
        this.side = checkNotNull(side, "null side");
    }

    public long getUserOrderId() {
        return userOrderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("systemOrderId", getSystemOrderId())
                .add("userOrderId", userOrderId)
                .add("timestamp", timestamp)
                .add("side", side)
                .add("initialQuantity", getInitialQuantity())
                .add("quantity", getQuantityLeft())
                .add("price", getPrice())
                .toString();
    }
}
