package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.LimitOrderSpec;
import com.quedex.marketmaker.qdxapi.entities.OrderSide;

import java.math.BigDecimal;

public class GenericOrder {

    private final String symbol;
    private final OrderSide side;
    private final BigDecimal price;
    private final int quantity;
    private final int initialQuantity;

    public GenericOrder(String symbol, OrderSide side, BigDecimal price, int initialQuantity) {
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = initialQuantity;
        this.initialQuantity = initialQuantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public int getFilledQuantity() {
        return initialQuantity - quantity;
    }

    public LimitOrderSpec toLimitOrderSpec(long clientOrderId) {
        return new LimitOrderSpec(
                clientOrderId,
                symbol,
                side,
                initialQuantity,
                price
        );
    }
}
