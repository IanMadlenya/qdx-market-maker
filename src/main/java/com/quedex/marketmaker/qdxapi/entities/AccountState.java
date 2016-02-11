package com.quedex.marketmaker.qdxapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
public final class AccountState {

    private final long accountId;
    private final String keyFingerprint;
    private final String email;
    private final String btcAddress;
    private final BigDecimal balance;
    private final BigDecimal freeBalance;
    private final BigDecimal totalInitialMargin;
    private final BigDecimal totalMaintenanceMargin;
    private final BigDecimal totalLockedForOrders;
    private final BigDecimal totalUnsettledPnL;
    private final ImmutableMap<String, PendingOrders> pendingOrders;
    private final ImmutableMap<String, OpenPositionInfo> openPositions;

    @JsonCreator
    public AccountState(
            @JsonProperty("account_id") long accountId,
            @JsonProperty("key_fingerprint") String keyFingerprint,
            @JsonProperty("email") String email,
            @JsonProperty("deposit_address") String btcAddress,
            @JsonProperty("balance") BigDecimal balance,
            @JsonProperty("free_balance") BigDecimal freeBalance,
            @JsonProperty("total_initial_margin") BigDecimal totalInitialMargin,
            @JsonProperty("total_maintenance_margin") BigDecimal totalMaintenanceMargin,
            @JsonProperty("total_locked_for_orders") BigDecimal totalLockedForOrders,
            @JsonProperty("total_unsettled_pnl") BigDecimal totalUnsettledPnL,
            @JsonProperty("pending_orders") ImmutableMap<String, PendingOrders> pendingOrders,
            @JsonProperty("open_positions") ImmutableMap<String, OpenPositionInfo> openPositions
    ) {
        checkArgument(!keyFingerprint.isEmpty());
        checkNotNull(email);

        this.accountId = accountId;
        this.keyFingerprint = keyFingerprint;
        this.email = email;
        this.btcAddress = checkNotNull(btcAddress);
        this.balance = checkNotNull(balance);
        this.freeBalance = checkNotNull(freeBalance);
        this.totalInitialMargin = checkNotNull(totalInitialMargin);
        this.totalMaintenanceMargin = checkNotNull(totalMaintenanceMargin);
        this.totalLockedForOrders = checkNotNull(totalLockedForOrders);
        this.totalUnsettledPnL = checkNotNull(totalUnsettledPnL);
        this.pendingOrders = checkNotNull(pendingOrders);
        this.openPositions = checkNotNull(openPositions);
    }

    public long getAccountId() {
        return accountId;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    public String getEmail() {
        return email;
    }

    public String getBTCAddress() {
        return btcAddress;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getFreeBalance() {
        return freeBalance;
    }

    public BigDecimal getTotalInitialMargin() {
        return totalInitialMargin;
    }

    public BigDecimal getTotalMaintenanceMargin() {
        return totalMaintenanceMargin;
    }

    public BigDecimal getTotalLockedForOrders() {
        return totalLockedForOrders;
    }

    public BigDecimal getTotalUnsettledPnL() {
        return totalUnsettledPnL;
    }

    public ImmutableMap<String, PendingOrders> getPendingOrders() {
        return pendingOrders;
    }

    public ImmutableMap<String, OpenPositionInfo> getOpenPositions() {
        return openPositions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("keyFingerprint", keyFingerprint)
                .add("email", email)
                .add("btcAddress", btcAddress)
                .add("balance", balance)
                .add("freeBalance", freeBalance)
                .add("totalInitialMargin", totalInitialMargin)
                .add("totalMaintenanceMargin", totalMaintenanceMargin)
                .add("totalLockedForOrders", totalLockedForOrders)
                .add("totalUnsettledPnL", totalUnsettledPnL)
                .add("pendingOrders", pendingOrders)
                .add("openPositions", openPositions)
                .toString();
    }
}
