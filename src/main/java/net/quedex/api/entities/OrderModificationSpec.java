package net.quedex.api.entities;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public final class OrderModificationSpec {

    private final long clientOrderId;
    private final int newQuantity;
    private final BigDecimal newLimitPrice;

    public OrderModificationSpec(long clientOrderId, int newQuantity, BigDecimal newLimitPrice) {
        checkArgument(newQuantity > 0, "newQuantity=%s <= 0", newQuantity);
        checkArgument(newLimitPrice.compareTo(BigDecimal.ZERO) > 0, "limitPrice=%s <= 0", newLimitPrice);
        this.clientOrderId = clientOrderId;
        this.newQuantity = newQuantity;
        this.newLimitPrice = newLimitPrice;
    }

    public long getClientOrderId() {
        return clientOrderId;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public BigDecimal getNewLimitPrice() {
        return newLimitPrice;
    }
}
