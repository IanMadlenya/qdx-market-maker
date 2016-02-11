package com.quedex.marketmaker.qdxapi.endpoint;

import com.quedex.marketmaker.qdxapi.pgp.BcPrivateKey;
import com.quedex.marketmaker.qdxapi.pgp.BcPublicKey;
import com.quedex.marketmaker.qdxapi.pgp.PGPKeyInitialisationException;

import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

public class QdxEndpointProvider {

    private final QdxEndpointProviderConfiguration configuration;

    public QdxEndpointProvider(QdxEndpointProviderConfiguration configuration) {
        this.configuration = checkNotNull(configuration);
    }

    public QdxEndpoint getQdxEndPoint() {
        try {
            return new QdxEndpoint(
                    configuration.getAccountId(),
                    configuration.getAccountNonceGroup(),
                    new QdxConnector(new AuthQdxHttpRequestFactory(
                            new URL(configuration.getQuedexBaseUrl()),
                            configuration.getQuedexUserName(),
                            configuration.getQuedexPassword()
                    )),
                    new QdxCryptService(
                            new BcPrivateKey(configuration.getAccountKey(), configuration.getAccountKeyPassword()),
                            new BcPublicKey(configuration.getQuedexPublicKey())
                    )
            );
        } catch (MalformedURLException | PGPKeyInitialisationException e) {
            throw new IllegalStateException("Failed to initialise qdx endpoint from config file", e);
        }
    }
}
