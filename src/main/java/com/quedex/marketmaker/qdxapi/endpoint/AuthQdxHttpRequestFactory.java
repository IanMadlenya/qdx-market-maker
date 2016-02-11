package com.quedex.marketmaker.qdxapi.endpoint;

import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.MultipartBody;

import java.net.URL;

public class AuthQdxHttpRequestFactory extends QdxHttpRequestFactory {

    private final String username;
    private final String password;

    public AuthQdxHttpRequestFactory(URL quedexUrl, String username, String password) {
        super(quedexUrl);
        this.username = username;
        this.password = password;
    }

    @Override
    public MultipartBody post(String spec) {
        return super.post(spec).basicAuth(username, password);
    }

    @Override
    public GetRequest get(String url) {
        return super.get(url).basicAuth(username, password);
    }
}
