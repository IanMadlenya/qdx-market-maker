package com.quedex.qdxapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class QdxConnector {

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new GuavaModule());

    private final HttpRequestFactory httpRequestFactory;

    public QdxConnector(HttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = checkNotNull(httpRequestFactory, "null httpRequestFactory");
    }

    public String send(String message) throws CommunicationException {
        try {
            HttpResponse<String> response = httpRequestFactory.post("/pro/send_order?with_response=1")
                    .field("encrypted_order", message)
                    .asString();
            checkResponse(response);
            JsonNode responses = MAPPER.readTree(response.getBody());

            Iterator<JsonNode> iNode = responses.iterator();
            checkState(iNode.hasNext());

            JsonNode node = iNode.next();
            checkNotNull(node.get("has_error"));
            checkNotNull(node.get("content"));

            if (node.get("has_error").asBoolean()) {
                // indicates programming error on our side
                throw new IllegalStateException("Response with error:" + node.get("error_message"));
            }

            return node.get("content").asText();

        } catch (UnirestException e) {
            throw new CommunicationException("Error sending message", e);
        } catch (IOException e) {
            throw new CommunicationException("Error parsing response", e);
        }
    }

    public String getSpotData() throws CommunicationException {
        return callDataUrl("/spot_data");
    }

    public String getInstrumentData() throws CommunicationException {
        return callDataUrl("/instrument_data");
    }

    private String callDataUrl(String spec) throws CommunicationException {
        try {
            HttpResponse<String> response = this.httpRequestFactory.get(spec).asString();
            checkResponse(response);
            return response.getBody();
        } catch (UnirestException e) {
            throw new CommunicationException("Error getting data", e);
        }
    }

    private static void checkResponse(HttpResponse<?> response) throws CommunicationException {
        if (response.getHeaders().containsKey("X_DOWN_FOR_MAINTENANCE")) {
            throw new CommunicationException("Quedex is down for maintenance - cannot send orders");
        }
        if (response.getStatus() != HttpURLConnection.HTTP_MOVED_TEMP && response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new CommunicationException("Response error: " + response.getStatus() + " - " + response.getStatusText());
        }
    }

    public static void main(String... args) throws Exception {
        QdxConnector qdxConnector = new QdxConnector(new QdxHttpRequestFactory(new URL("http://plaintext.quedex.net")));
        System.out.println(qdxConnector.getInstrumentData());
        System.out.println(qdxConnector.getSpotData());
    }
}
