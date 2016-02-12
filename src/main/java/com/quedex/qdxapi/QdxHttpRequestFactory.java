package com.quedex.qdxapi;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.MultipartBody;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;

import java.net.MalformedURLException;
import java.net.URL;

public class QdxHttpRequestFactory implements HttpRequestFactory {

    static {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10_000)
                .setSocketTimeout(10_000)
                .build();

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        Unirest.setHttpClient(client);
    }

    private final URL quedexUrl;

    public QdxHttpRequestFactory(URL quedexUrl) {
        this.quedexUrl = quedexUrl;
    }

    @Override
    public MultipartBody post(String spec) {
        try {
            return Unirest.post(new URL(quedexUrl, spec).toString())
                    .fields(ImmutableMap.of()); // no-op to return MultipartBody
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public GetRequest get(String spec) {
        try {
            return Unirest.get(new URL(quedexUrl, spec).toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
