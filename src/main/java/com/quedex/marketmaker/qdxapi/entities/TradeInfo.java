package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class TradeInfo {

    private final long tradeId;
    private final long timestamp;
    private final BigDecimal price;
    private final int quantity; // may be 0 when reference trade

    private final long orderIdFirst;
    private final long orderIdSecond;

    @JsonCreator
    public TradeInfo(
            @JsonProperty("trade_id") long tradeId,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("system_order_id_first") long orderIdFirst,
            @JsonProperty("system_order_id_second") long orderIdSecond
    ) {
        this.tradeId = tradeId;
        this.timestamp = timestamp;
        this.price = price;
        this.quantity = quantity;
        this.orderIdFirst = orderIdFirst;
        this.orderIdSecond = orderIdSecond;
    }

    public long getTradeId() {
        return tradeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getOrderIdFirst() {
        return orderIdFirst;
    }

    public long getOrderIdSecond() {
        return orderIdSecond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TradeInfo that = (TradeInfo) o;

        return this.tradeId == that.tradeId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tradeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tradeId", tradeId)
                .add("timestamp", timestamp)
                .add("price", price)
                .add("quantity", quantity)
                .add("orderIdFirst", orderIdFirst)
                .add("orderIdSecond", orderIdSecond)
                .toString();
    }
}
