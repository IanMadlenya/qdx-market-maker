package net.quedex.marketmaker;

import net.quedex.api.entities.LimitOrderSpec;
import net.quedex.api.entities.OrderSide;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GenericOrder {

    private final String symbol;
    private final OrderSide side;
    private final BigDecimal price;
    private final int quantity;
    private final int initialQuantity;

    public GenericOrder(String symbol, OrderSide side, BigDecimal price, int initialQuantity) {
        checkArgument(!symbol.isEmpty(), "Empty symbol");
        checkArgument(price.compareTo(BigDecimal.ZERO) > 0, "price=%s <= 0", price);
        checkArgument(initialQuantity > 0, "initialQuantity=%s <= 0", initialQuantity);
        this.symbol = symbol;
        this.side = checkNotNull(side, "null side");
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