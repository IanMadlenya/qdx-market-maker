package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class BuySellBook {

    // price-time priority comparators for orders
    private static final Comparator<OrderInfo> BUYS_COMPARATOR = (o1, o2) -> {
        if (!o1.getPrice().isPresent()) { // market
            if (!o2.getPrice().isPresent()) {
                return Long.compare(o1.getSystemOrderId(), o2.getSystemOrderId());
            }
            return -1;
        }
        if (!o2.getPrice().isPresent()) {
            return 1;
        }
        if (o1.getPrice().get().compareTo(o2.getPrice().get()) == 0) {
            return Long.compare(o1.getSystemOrderId(), o2.getSystemOrderId());
        }
        return -o1.getPrice().get().compareTo(o2.getPrice().get());
    };
    private static final Comparator<OrderInfo> SELLS_COMPARATOR = (o1, o2) -> {
        if (!o1.getPrice().isPresent()) { // market
            if (!o2.getPrice().isPresent()) {
                return Long.compare(o1.getSystemOrderId(), o2.getSystemOrderId());
            }
            return -1;
        }
        if (!o2.getPrice().isPresent()) {
            return 1;
        }
        if (o1.getPrice().get().compareTo(o2.getPrice().get()) == 0) {
            return Long.compare(o1.getSystemOrderId(), o2.getSystemOrderId());
        }
        return o1.getPrice().get().compareTo(o2.getPrice().get());
    };

    private final ImmutableList<OrderInfo> buys;
    private final ImmutableList<OrderInfo> sells;

    @JsonCreator
    public BuySellBook(
            @JsonProperty("buy_orders") ArrayList<OrderInfo> buys,
            @JsonProperty("sell_orders") ArrayList<OrderInfo> sells
    ) {
        buys.sort(BUYS_COMPARATOR);
        sells.sort(SELLS_COMPARATOR);
        this.buys = ImmutableList.copyOf(buys);
        this.sells = ImmutableList.copyOf(sells);
    }

    /**
     * @return list of buy orders (best first)
     */
    public ImmutableList<OrderInfo> getBuys() {
        return buys;
    }

    /**
     * @return list of sell orders (best first)
     */
    public ImmutableList<OrderInfo> getSells() {
        return sells;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("buys", buys)
                .add("sells", sells)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuySellBook that = (BuySellBook) o;

        return Objects.equal(this.buys, that.buys) &&
                Objects.equal(this.sells, that.sells);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(buys, sells);
    }
}
