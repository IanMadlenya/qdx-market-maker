package com.quedex.marketmaker;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.math.BigDecimal;

public class MarketMakerConfiguration {

    private final int maxConnectionRetry;
    private final int timeSleepSeconds;

    private final BigDecimal spreadFraction;
    private final BigDecimal fairPriceSensitivityFraction;
    private final int numLevels;
    private final int qtyOnLevel;
    private final double deltaLimit;

    public MarketMakerConfiguration(
            int maxConnectionRetry,
            int timeSleepSeconds,
            BigDecimal spreadFraction,
            BigDecimal fairPriceSensitivityFraction,
            int numLevels,
            int qtyOnLevel,
            double deltaLimit
    ) {
        this.maxConnectionRetry = maxConnectionRetry;
        this.timeSleepSeconds = timeSleepSeconds;
        this.spreadFraction = spreadFraction;
        this.fairPriceSensitivityFraction = fairPriceSensitivityFraction;
        this.numLevels = numLevels;
        this.qtyOnLevel = qtyOnLevel;
        this.deltaLimit = deltaLimit;
    }

    public static MarketMakerConfiguration fromPropertiesFile(String fileName) throws ConfigurationException {
        Configuration configuration = new PropertiesConfiguration(fileName);
        return new MarketMakerConfiguration(
                configuration.getInt(ConfigKey.MAX_CONNECTION_RETRY.getKey()),
                configuration.getInt(ConfigKey.TIME_SLEEP_SECONDS.getKey()),
                new BigDecimal(configuration.getString(ConfigKey.SPREAD_FRACTION.getKey())),
                new BigDecimal(configuration.getString(ConfigKey.FAIR_PRICE_SENSITIVITY_FRACTION.getKey())),
                configuration.getInt(ConfigKey.NUM_LEVELS.getKey()),
                configuration.getInt(ConfigKey.QUANTITY_ON_LEVEL.getKey()),
                configuration.getDouble(ConfigKey.DELTA_LIMIT.getKey())
        );
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

    public BigDecimal getFairPriceSensitivityFraction() {
        return fairPriceSensitivityFraction;
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

    private enum ConfigKey {
        MAX_CONNECTION_RETRY("maxConnectionRetry"),
        TIME_SLEEP_SECONDS("timeSleepSeconds"),
        SPREAD_FRACTION("spreadFraction"),
        FAIR_PRICE_SENSITIVITY_FRACTION("fairPriceSensitivityFraction"),
        NUM_LEVELS("numLevels"),
        QUANTITY_ON_LEVEL("quantityOnLevel"),
        DELTA_LIMIT("deltaLimit");

        private static final String COMMON_PREFIX = "com.quedex.marketmaker.qdxapi";
        private static final char SEPARATOR = '.';

        public static String getCommonPrefix() {
            return COMMON_PREFIX;
        }

        private final String keyFragment;

        ConfigKey(String keyFragment) {
            this.keyFragment = keyFragment;
        }

        public String getKey() {
            return COMMON_PREFIX + SEPARATOR + keyFragment;
        }
    }
}
