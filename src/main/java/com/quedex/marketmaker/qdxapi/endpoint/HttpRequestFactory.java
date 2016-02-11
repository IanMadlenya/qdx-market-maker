package com.quedex.marketmaker.qdxapi.endpoint;

import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.MultipartBody;

public interface HttpRequestFactory {

    MultipartBody post(String url);

    GetRequest get(String url);
}
