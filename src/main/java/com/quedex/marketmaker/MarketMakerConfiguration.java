package com.quedex.marketmaker;

import java.math.BigDecimal;

public class MarketMakerConfiguration {

    private final int maxConnectionRetry;
    private final int timeSleepSeconds;

    private final BigDecimal spreadFraction;
    private final int numLevels;
    private final int qtyOnLevel;
    private final double deltaLimit;

    public MarketMakerConfiguration(int maxConnectionRetry, int timeSleepSeconds, BigDecimal spreadFraction, int numLevels, int qtyOnLevel, double deltaLimit) {
        this.maxConnectionRetry = maxConnectionRetry;
        this.timeSleepSeconds = timeSleepSeconds;
        this.spreadFraction = spreadFraction;
        this.numLevels = numLevels;
        this.qtyOnLevel = qtyOnLevel;
        this.deltaLimit = deltaLimit;
    }

    public int getMaxConnectionRetry() {
        return maxConnectionRetry;
    }

    public int getTimeSleepSeconds() {
        return timeSleepSeconds;
    }

    public BigDecimal getSpreadFraction() {
        return spreadFraction;
    }

    public int getNumLevels() {
        return numLevels;
    }

    public int getQtyOnLevel() {
        return qtyOnLevel;
    }

    public double getDeltaLimit() {
        return deltaLimit;
    }
}
