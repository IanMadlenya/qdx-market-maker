package net.quedex.api;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkArgument;

public final class QdxEndpointProviderConfiguration {

    private final String quedexBaseUrl;
    private final String quedexUserName;
    private final String quedexPassword;
    private final String quedexPublicKey;

    private final long accountId;
    private final String accountKey;
    private final String accountKeyPassword;
    private final int accountNonceGroup;

    public QdxEndpointProviderConfiguration(
            String quedexBaseUrl,
            String quedexUserName,
            String quedexPassword,
            String quedexPublicKey,
            long accountId,
            String accountKey,
            String accountKeyPassword,
            int accountNonceGroup
    ) {
        checkArgument(!Strings.isNullOrEmpty(quedexBaseUrl));
        checkArgument(!Strings.isNullOrEmpty(quedexPublicKey));
        checkArgument(!Strings.isNullOrEmpty(accountKey));
        this.quedexBaseUrl = quedexBaseUrl;
        this.quedexUserName = quedexUserName;
        this.quedexPassword = quedexPassword;
        this.quedexPublicKey = quedexPublicKey;
        this.accountId = accountId;
        this.accountKey = accountKey;
        this.accountKeyPassword = accountKeyPassword;
        this.accountNonceGroup = accountNonceGroup;
    }

    public String getQuedexBaseUrl() {
        return quedexBaseUrl;
    }

    public String getQuedexUserName() {
        return quedexUserName;
    }

    public String getQuedexPassword() {
        return quedexPassword;
    }

    public String getQuedexPublicKey() {
        return quedexPublicKey;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public String getAccountKeyPassword() {
        return accountKeyPassword;
    }

    public int getAccountNonceGroup() {
        return accountNonceGroup;
    }
}
