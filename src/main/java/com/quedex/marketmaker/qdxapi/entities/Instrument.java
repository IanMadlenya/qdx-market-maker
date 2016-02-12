package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains all information about a trading instrument.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class Instrument {

    public enum SettlementMethod {
        FINANCIAL, PHYSICAL
    }

    private final String symbol;
    private final int instrumentId;
    private final InstrumentType instrumentType;
    private final BigDecimal tickSize;
    private final long issueDate;
    private final long expirationDate;
    private final String underlyingSymbol;
    private final int notionalAmount;
    private final SettlementMethod settlementMethod;
    private final BigDecimal feeFraction;
    private final BigDecimal takerToMakerFeeFraction;
    private final BigDecimal initialMarginFraction;
    private final BigDecimal maintenanceMarginFraction;
    private final Optional<Long> firstNoticeDate;             // futures only
    private final Optional<BigDecimal> strike;                // option only

    @JsonCreator
    public Instrument(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("instrument_id") int instrumentId,
            @JsonProperty("type") Type type,
            @JsonProperty("option_type") OptionType optionType,
            @JsonProperty("tick_size") BigDecimal tickSize,
            @JsonProperty("issue_date") long issueDate,
            @JsonProperty("expiration_date") long expirationDate,
            @JsonProperty("underlying_symbol") String underlyingSymbol,
            @JsonProperty("notional_amount") int notionalAmount,
            @JsonProperty("settlement_method") SettlementMethod settlementMethod,
            @JsonProperty("fee") BigDecimal feeFraction,
            @JsonProperty("taker_to_maker") BigDecimal takerToMaker,
            @JsonProperty("initial_margin") BigDecimal initialMarginFraction,
            @JsonProperty("maintenance_margin") BigDecimal maintenanceMarginFraction,
            @JsonProperty("first_notice_date") Long firstNoticeDate,
            @JsonProperty("strike") BigDecimal strike
    ) {
        checkArgument(!symbol.isEmpty(), "Empty symbol");
        checkArgument(tickSize.compareTo(BigDecimal.ZERO) > 0, "tickSize=%s <=0", tickSize);
        checkArgument(issueDate > 0, "issueDate=%s <= 0", issueDate);
        checkArgument(expirationDate > 0, "expirationDate=%s <= 0", issueDate);
        checkArgument(!underlyingSymbol.isEmpty(), "Empty underlyingSymbol");
        checkArgument(notionalAmount > 0, "notionalAmount=%s <= 0", notionalAmount);
        checkArgument(feeFraction.compareTo(BigDecimal.ZERO) >= 0, "takerToMaker=%s < 0", takerToMaker);
        checkArgument(takerToMaker.compareTo(BigDecimal.ZERO) >= 0, "takerToMaker=%s < 0", takerToMaker);
        checkArgument(initialMarginFraction.compareTo(BigDecimal.ZERO) >= 0,
                "initialMarginFraction=%s < 0", initialMarginFraction);
        checkArgument(maintenanceMarginFraction.compareTo(BigDecimal.ZERO) >= 0,
                "maintenanceMarginFraction=%s < 0", maintenanceMarginFraction);

        this.symbol = symbol;
        this.instrumentId = instrumentId;
        if (type == Type.futures) {
            this.instrumentType = InstrumentType.FUTURES;
        } else {
            this.instrumentType = optionType == OptionType.CALL_EUROPEAN
                    ? InstrumentType.OPTION_EUROPEAN_CALL
                    : InstrumentType.OPTION_EUROPEAN_PUT;
        }
        this.tickSize = tickSize;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.underlyingSymbol = underlyingSymbol;
        this.notionalAmount = notionalAmount;
        this.settlementMethod = checkNotNull(settlementMethod, "Null settlementMethod");
        this.feeFraction = feeFraction;
        this.takerToMakerFeeFraction = takerToMaker;
        this.initialMarginFraction = initialMarginFraction;
        this.maintenanceMarginFraction = maintenanceMarginFraction;

        switch (instrumentType) {
            case FUTURES:
                checkArgument(firstNoticeDate > 0, "firstNoticeDate=%s <= 0", firstNoticeDate);
                checkArgument(strike == null, "Expected null strike");
                this.firstNoticeDate = Optional.of(firstNoticeDate);
                this.strike = Optional.empty();
                break;
            case OPTION_EUROPEAN_CALL:
            case OPTION_EUROPEAN_PUT:
                checkArgument(strike.compareTo(BigDecimal.ZERO) > 0, "strike=%s <= 0", strike);
                this.strike = Optional.of(strike);
                this.firstNoticeDate = Optional.empty();
                break;
            default:
                throw new IllegalStateException("Unsupported instrument type");
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public int getNotionalAmount() {
        return notionalAmount;
    }

    public SettlementMethod getSettlementMethod() {
        return settlementMethod;
    }

    public BigDecimal getFeeFraction() {
        return feeFraction;
    }

    public BigDecimal getTakerToMakerFeeFraction() {
        return takerToMakerFeeFraction;
    }

    /**
     * @return taker fee fraction
     */
    public BigDecimal getTakerFeeFraction() {
        return feeFraction.add(takerToMakerFeeFraction);
    }

    /**
     * @return maker fee fraction (may be negative)
     */
    public BigDecimal getMakerFeeFraction() {
        return feeFraction.subtract(takerToMakerFeeFraction);
    }

    public BigDecimal getInitialMarginFraction() {
        return initialMarginFraction;
    }

    public BigDecimal getMaintenanceMarginFraction() {
        return maintenanceMarginFraction;
    }

    public Optional<Long> getFirstNoticeDate() {
        return firstNoticeDate;
    }

    public Optional<BigDecimal> getStrike() {
        return strike;
    }

    public boolean isFutures() {
        return instrumentType == InstrumentType.FUTURES;
    }

    public boolean isTraded(long currentTime) {
        return issueDate < currentTime && currentTime < expirationDate;
    }

    /**
     * @return price rounded down to tick size or tick size if it would be negative
     */
    public BigDecimal roundPriceToTickSide(BigDecimal price) {
        BigDecimal remainder = price.remainder(tickSize);
        BigDecimal result = price.subtract(remainder);
        if (remainder.compareTo(BigDecimal.ZERO) < 0) {
            return tickSize;
        }
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("instrumentId", instrumentId)
                .add("instrumentType", instrumentType)
                .add("tickSize", tickSize)
                .add("issueDate", issueDate)
                .add("expirationDate", expirationDate)
                .add("underlyingSymbol", underlyingSymbol)
                .add("notionalAmount", notionalAmount)
                .add("settlementMethod", settlementMethod)
                .add("feeFraction", feeFraction)
                .add("takerToMakerFeeFraction", takerToMakerFeeFraction)
                .add("initialMarginFraction", initialMarginFraction)
                .add("maintenanceMarginFraction", maintenanceMarginFraction)
                .add("firstNoticeDate", firstNoticeDate)
                .add("strike", strike)
                .toString();
    }

    // used only to decode JSON
    private enum Type { futures, option }
    private enum OptionType { CALL_EUROPEAN, PUT_EUROPEAN }
}
