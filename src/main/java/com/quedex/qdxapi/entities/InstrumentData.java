package com.quedex.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class InstrumentData {

    public enum InstrumentSessionState {
        INACTIVE, CONTINUOUS, AUCTION, NO_TRADING
    }

    public enum GlobalSessionState {
        NO_TRADING, OPENING_AUCTION, AUCTION, CONTINUOUS, CLOSING_AUCTION, MAINTENANCE
    }


    private final ImmutableMap<String, ImmutableList<TradeInfo>> trades;
    private final ImmutableMap<String, BuySellBook> orderBook;
    private final ImmutableMap<String, Instrument> instrumentInfo;
    private final ImmutableMap<String, InstrumentSessionState> sessionStates;
    private final GlobalSessionState globalSessionState;
    private final long updateTime;
    private final ImmutableMap<String, BigInteger> openInterests;
    private final ImmutableMap<String, BigDecimal> taps;

    @JsonCreator
    public InstrumentData(
            @JsonProperty("trades") ImmutableMap<String, ImmutableList<TradeInfo>> trades,
            @JsonProperty("order_book") ImmutableMap<String, BuySellBook> orderBook,
            @JsonProperty("info") ImmutableMap<String, Instrument> instrumentInfo,
            @JsonProperty("session_states") ImmutableMap<String, InstrumentSessionState> sessionStates,
            @JsonProperty("global_session_state") GlobalSessionState globalSessionState,
            @JsonProperty("update_time") long updateTime,
            @JsonProperty("open_interest") ImmutableMap<String, BigInteger> openInterests,
            @JsonProperty("tap") ImmutableMap<String, BigDecimal> taps
    ) {
        checkArgument(updateTime > 0, "Expected positive updateTime but was: %s", updateTime);

        this.trades = checkNotNull(trades, "null trades");
        this.orderBook = checkNotNull(orderBook, "null orderBook");
        this.instrumentInfo = checkNotNull(instrumentInfo, "inull nstrumentInfo");
        this.sessionStates = checkNotNull(sessionStates, "null sessionStates");
        this.globalSessionState = checkNotNull(globalSessionState, "null globalSessionState");
        this.updateTime = updateTime;
        this.openInterests = checkNotNull(openInterests, "null openInterests");
        this.taps = checkNotNull(taps, "null taps");
    }

    public ImmutableMap<String, ImmutableList<TradeInfo>> getTrades() {
        return trades;
    }

    public ImmutableMap<String, BuySellBook> getOrderBook() {
        return orderBook;
    }

    public ImmutableMap<String, Instrument> getInstrumentInfo() {
        return instrumentInfo;
    }

    public ImmutableMap<String, InstrumentSessionState> getSessionStates() {
        return sessionStates;
    }

    public GlobalSessionState getGlobalSessionState() {
        return globalSessionState;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public ImmutableMap<String, BigInteger> getOpenInterests() {
        return openInterests;
    }

    public ImmutableMap<String, BigDecimal> getTaps() {
        return taps;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("trades", trades)
                .add("orderBook", orderBook)
                .add("instrumentInfo", instrumentInfo)
                .add("sessionStates", sessionStates)
                .add("globalSessionState", globalSessionState)
                .add("updateTime", updateTime)
                .add("openInterests", openInterests)
                .add("taps", taps)
                .toString();
    }
}
