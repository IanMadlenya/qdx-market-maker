package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class OpenPositionInfo {

    private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();

    public enum Side {
        LONG {
            @Override
            public BigDecimal value() {
                return BigDecimal.ONE;
            }
        }, SHORT {
            @Override
            public BigDecimal value() {
                return MINUS_ONE;
            }
        };

        public abstract BigDecimal value();
    }

    private final BigDecimal initialMargin;
    private final BigDecimal maintenanceMargin;
    private final Side positionSide;
    private final int positionQuantity;           // always >= 0
    private final Optional<BigDecimal> pnl;       // futures only
    private final BigDecimal averageOpeningPrice;

    public OpenPositionInfo(
            @JsonProperty("initial_margin") BigDecimal initialMargin,
            @JsonProperty("maintenance_margin") BigDecimal maintenanceMargin,
            @JsonProperty("side") Side positionSide,
            @JsonProperty("quantity") int positionQuantity,
            @JsonProperty("PnL") BigDecimal pnl,
            @JsonProperty("average_opening_price") BigDecimal averageOpeningPrice
    ) {
        this.initialMargin = checkNotNull(initialMargin);
        this.maintenanceMargin = checkNotNull(maintenanceMargin);
        this.positionSide = checkNotNull(positionSide);
        this.positionQuantity = positionQuantity;
        this.pnl = Optional.ofNullable(pnl);
        this.averageOpeningPrice = checkNotNull(averageOpeningPrice);
    }

    public BigDecimal getInitialMargin() {
        return initialMargin;
    }

    public BigDecimal getMaintenanceMargin() {
        return maintenanceMargin;
    }

    public Side getPositionSide() {
        return positionSide;
    }

    public long getPositionQuantity() {
        return positionQuantity;
    }

    public Optional<BigDecimal> getPnl() {
        return pnl;
    }

    public BigDecimal getAverageOpeningPrice() {
        return averageOpeningPrice;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("initialMargin", initialMargin)
                .add("maintenanceMargin", maintenanceMargin)
                .add("positionSide", positionSide)
                .add("positionQuantity", positionQuantity)
                .add("pnl", pnl)
                .add("averageOpeningPrice", averageOpeningPrice)
                .toString();
    }
}
