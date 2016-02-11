package com.quedex.marketmaker.qdxapi.endpoint;

import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

public final class QdxEndpointProviderConfigFactory {

    private final QdxEndpointProviderConfiguration configuration;

    public QdxEndpointProviderConfigFactory(String resourceName) {
        checkArgument(!Strings.isNullOrEmpty(resourceName));
        try {
            this.configuration = createConfiguration(resourceName);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the configuration from " + resourceName +
                    ". Make sure it does not contain invalid data.", e);
        }
    }

    private QdxEndpointProviderConfiguration createConfiguration(String resourceName) throws Exception {
        URL resourceUrl = Resources.getResource(resourceName);
        Configuration configuration = new PropertiesConfiguration(resourceUrl);
        return new QdxEndpointProviderConfiguration(
                configuration.getString(QdxEndPointProviderConfigKey.QUEDEX_BASE_URL.getKey()),
                configuration.getString(QdxEndPointProviderConfigKey.QUEDEX_USER_NAME.getKey()),
                configuration.getString(QdxEndPointProviderConfigKey.QUEDEX_PASSWORD.getKey()),
                configuration.getString(QdxEndPointProviderConfigKey.QUEDEX_PUBLIC_KEY.getKey()),
                configuration.getLong(QdxEndPointProviderConfigKey.ACCOUNT_ID.getKey()),
                configuration.getString(QdxEndPointProviderConfigKey.ACCOUNT_KEY.getKey()),
                configuration.getString(QdxEndPointProviderConfigKey.ACCOUNT_PASSWORD.getKey()),
                configuration.getInt(QdxEndPointProviderConfigKey.ACCOUNT_NONCE_GROUP.getKey(), 0) // defaults to 0
        );
    }

    public QdxEndpointProviderConfiguration getConfiguration() {
        return configuration;
    }
}
