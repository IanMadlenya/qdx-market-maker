package net.quedex.api.entities;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class LimitOrderSpec {

    private final long clientOrderId;
    private final String symbol;
    private final OrderSide side;
    private final int quantity;
    private final BigDecimal limitPrice;

    public LimitOrderSpec(long clientOrderId, String symbol, OrderSide side, int quantity, BigDecimal limitPrice) {
        checkArgument(quantity > 0, "quantity=%s <= 0", quantity);
        checkArgument(limitPrice.compareTo(BigDecimal.ZERO) > 0, "limitPrice=%s <= 0", limitPrice);
        this.clientOrderId = clientOrderId;
        this.symbol = symbol;
        this.side = checkNotNull(side, "Null side");
        this.quantity = quantity;
        this.limitPrice = limitPrice;
    }

    public long getClientOrderId() {
        return clientOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    @Override
    public String toString() {
        return symbol + ": " + side + " " + quantity + " @ " + limitPrice;
    }
}
