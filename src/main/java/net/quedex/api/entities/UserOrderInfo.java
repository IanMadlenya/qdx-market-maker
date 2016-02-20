package net.quedex.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class UserOrderInfo extends OrderInfo {

    private final long clientOrderId;
    private final long timestamp;

    @JsonCreator
    public UserOrderInfo(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("system_order_id") long systemOrderId,
            @JsonProperty("order_id") long clientOrderId,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("initial_quantity") int initialQuantity,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("price") BigDecimal price
    ) {
        super(symbol, systemOrderId, price, side, quantity, initialQuantity);
        this.clientOrderId = clientOrderId;
        this.timestamp = timestamp;
    }

    public long getClientOrderId() {
        return clientOrderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", getSymbol())
                .add("systemOrderId", getSystemOrderId())
                .add("clientOrderId", clientOrderId)
                .add("timestamp", timestamp)
                .add("side", getSide())
                .add("initialQuantity", getInitialQuantity())
                .add("quantity", getQuantityLeft())
                .add("price", getPrice())
                .toString();
    }
}
