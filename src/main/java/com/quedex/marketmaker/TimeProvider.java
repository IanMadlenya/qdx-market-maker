package com.quedex.marketmaker;

@FunctionalInterface
public interface TimeProvider {

    long getCurrentTime();
}
