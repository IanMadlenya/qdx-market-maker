package net.quedex.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class SpotData {

    private final ImmutableMap<String, BigDecimal> spotPrices;
    private final ImmutableMap<String, BigDecimal> settlementPrices;
    private final long updateTime;

    public SpotData(
            @JsonProperty("spot_price") ImmutableMap<String, BigDecimal> spotPrices,
            @JsonProperty("settlement_price") ImmutableMap<String, BigDecimal> settlementPrices,
            @JsonProperty("update_time") long updateTime
    ) {
        checkArgument(updateTime > 0, "Expected positive updateTime but was: %s", updateTime);
        this.spotPrices = checkNotNull(spotPrices, "null spotPrices");
        this.settlementPrices = checkNotNull(settlementPrices, "null settlementPrices");
        this.updateTime = updateTime;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("spotPrices", spotPrices)
                .add("settlementPrices", settlementPrices)
                .add("updateTime", updateTime)
                .toString();
    }
}
