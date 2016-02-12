package com.quedex.marketmaker.qdxapi.endpoint;

public enum QdxEndPointProviderConfigKey {
    QUEDEX_BASE_URL("quedexBaseUrl"),
    QUEDEX_USER_NAME("quedexUserName"),
    QUEDEX_PASSWORD("quedexPassword"),
    QUEDEX_PUBLIC_KEY("quedexPublicKey"),
    ACCOUNT_ID("accountId"),
    ACCOUNT_KEY("accountKey"),
    ACCOUNT_PASSWORD("accountPassword"),
    ACCOUNT_NONCE_GROUP("accountNonceGroup");

    private static final String COMMON_PREFIX = "com.quedex.marketmaker.qdxapi";
    private static final char SEPARATOR = '.';

    public static String getCommonPrefix() {
        return COMMON_PREFIX;
    }

    private final String keyFragment;

    QdxEndPointProviderConfigKey(String keyFragment) {
        this.keyFragment = keyFragment;
    }

    public String getKey() {
        return COMMON_PREFIX + SEPARATOR + keyFragment;
    }
}
