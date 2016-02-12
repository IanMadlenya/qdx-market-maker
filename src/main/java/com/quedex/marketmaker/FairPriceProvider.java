package com.quedex.marketmaker;

import java.math.BigDecimal;

@FunctionalInterface
public interface FairPriceProvider {

    BigDecimal getFairPrice(String symbol);
}
