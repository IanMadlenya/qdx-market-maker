package com.quedex.marketmaker.qdxapi.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quedex.marketmaker.qdxapi.entities.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class QdxEndpoint {

    private static final ObjectMapper MAPPER = QdxConnector.MAPPER;

    private final long accountId;
    private final int nonceGroup;
    private final QdxConnector qdxConnector;
    private final QdxCryptService qdxCryptService;

    /**
     * We need to put sending of all the messages into single-threaded executor, because Quedex requires messages to
     * have strictly increasing nonces (anti-replay-attack).
     */
    private final ExecutorService messageSendingExecutor = Executors.newSingleThreadExecutor();

    private final AtomicLong nonce = new AtomicLong();
    private volatile boolean initialized = false;

    public QdxEndpoint(
            long accountId,
            int nonceGroup,
            QdxConnector qdxConnector,
            QdxCryptService qdxCryptService
    ) {
        this.accountId = accountId;
        this.nonceGroup = nonceGroup;
        this.qdxConnector = checkNotNull(qdxConnector, "null quedexConnector");
        this.qdxCryptService = checkNotNull(qdxCryptService, "null quedexCryptService");
    }

    public void initialize() {

        JsonNode message = MAPPER.getNodeFactory().objectNode()
                .put("type", "last_nonce")
                .put("nonce_group", nonceGroup)
                .put("account_id", accountId);

        try {
            String encryptedMessage = qdxCryptService.encrypt(message.toString());
            String encryptedResponse = qdxConnector.send(encryptedMessage);

            JsonNode node = MAPPER.readTree(qdxCryptService.decrypt(encryptedResponse));

            nonce.set(node.get("content").get("last_nonce").asLong());
        } catch (CryptServiceException | IOException e) {
            throw new IllegalStateException("Error initializing", e);
        }

        initialized = true;
    }

    private long getNonce() {
        return nonce.incrementAndGet();
    }

    public AccountState getAccountState() throws CommunicationException {
        JsonNode response = sendEncryptedMessageGetDecryptedResponse(
                () -> MAPPER.getNodeFactory().objectNode()
                        .put("type", "account_state")
                        .put("nonce", getNonce())
                        .put("nonce_group", nonceGroup)
                        .put("account_id", accountId)
        );
        checkState(!response.get("has_error").booleanValue(), response.get("error_message").textValue()); // programming error
        try {
            return MAPPER.treeToValue(response.get("content"), AccountState.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public OrderPlaceResult placeOrder(LimitOrderSpec order) throws CommunicationException {
        JsonNode response = sendEncryptedMessageGetDecryptedResponse(() -> orderCreateJson(order));
        return response.get("has_error").booleanValue()
                ? new OrderPlaceResult(response.get("error_message").textValue())
                : new OrderPlaceResult(getSystemOrderId(response));
    }

    public List<OrderPlaceResult> placeOrders(List<LimitOrderSpec> orders) throws CommunicationException {
        JsonNode batchResponse = batch(orders, this::orderCreateJson); // batch response cannot have error
        Iterable<JsonNode> responses = () -> batchResponse.get("content").get("responses").iterator();
        return StreamSupport.stream(responses.spliterator(), false)
                .map(jn -> jn.get("has_error").booleanValue()
                        ? new OrderPlaceResult(jn.get("error_message").textValue())
                        : new OrderPlaceResult(getSystemOrderId(jn))
                ).collect(Collectors.toList());
    }

    private static long getSystemOrderId(JsonNode orderPlacedResponse) {
        String[] responseSplit = orderPlacedResponse.get("content").asText().split("\\s+");
        return Long.valueOf(responseSplit[responseSplit.length - 1]); // last word is the id
    }

    private ObjectNode orderCreateJson(LimitOrderSpec order) {
        return MAPPER.getNodeFactory().objectNode()
                .put("type", "order_create")
                .put("nonce", getNonce())
                .put("nonce_group", nonceGroup)
                .put("account_id", accountId)
                .put("order_id", order.getClientOrderId())
                .put("symbol", order.getSymbol())
                .put("order_type", "limit")
                .put("price", order.getLimitPrice())
                .put("quantity", order.getQuantity())
                .put("side", order.getSide().toString());
    }

    /**
     * @return true iff cancel is successful (may fail only if the order does not exists)
     */
    public boolean cancelOrder(long clientOrderId) throws CommunicationException {
        JsonNode response = sendEncryptedMessageGetDecryptedResponse(() -> orderCancelJson(clientOrderId));
        return !response.get("has_error").booleanValue();
    }

    public List<Boolean> cancelOrders(List<Long> orders) throws CommunicationException {
        JsonNode batchResponse = batch(orders, this::orderCancelJson); // batch response cannot have error
        Iterable<JsonNode> responses = () -> batchResponse.get("content").get("responses").iterator();
        return StreamSupport.stream(responses.spliterator(), false)
                .map(jn -> !jn.get("has_error").booleanValue())
                .collect(Collectors.toList());
    }

    private JsonNode orderCancelJson(long clientOrderId) {
        return MAPPER.getNodeFactory().objectNode()
                .put("type", "order_cancel")
                .put("nonce", getNonce())
                .put("nonce_group", nonceGroup)
                .put("account_id", accountId)
                .put("order_id", clientOrderId);
    }

    public OrderModifyResult modifyOrder(OrderModificationSpec modifiedOrder) throws CommunicationException {
        JsonNode response = sendEncryptedMessageGetDecryptedResponse(() -> orderModifyJson(modifiedOrder));
        return response.get("has_error").booleanValue()
                ? new OrderModifyResult(response.get("error_message").textValue())
                : new OrderModifyResult();
    }

    public List<OrderModifyResult> modifyOrders(List<OrderModificationSpec> modifiedOrders) throws CommunicationException {
        JsonNode batchResponse = batch(modifiedOrders, this::orderModifyJson);
        Iterable<JsonNode> responses = () -> batchResponse.get("content").get("responses").iterator();
        return StreamSupport.stream(responses.spliterator(), false)
                .map(jn -> jn.get("has_error").booleanValue()
                        ? new OrderModifyResult(jn.get("error_message").textValue())
                        : new OrderModifyResult()
                ).collect(Collectors.toList());
    }

    private JsonNode orderModifyJson(OrderModificationSpec order) {
        return MAPPER.getNodeFactory().objectNode()
                .put("type", "order_modify")
                .put("nonce", getNonce())
                .put("nonce_group", nonceGroup)
                .put("account_id", accountId)
                .put("order_id", order.getClientOrderId())
                .put("new_price", order.getNewLimitPrice())
                .put("new_quantity", order.getNewQuantity());
    }

    private <T> JsonNode batch(List<T> objects, Function<T, JsonNode> jsonCreator) throws CommunicationException {
        return sendEncryptedMessageGetDecryptedResponse(
                () -> {
                    ArrayNode messages = MAPPER.getNodeFactory().arrayNode();
                    objects.forEach(o -> messages.add(jsonCreator.apply(o)));
                    return MAPPER.getNodeFactory().objectNode()
                            .put("type", "batch")
                            .set("messages", messages);
                }
        );
    }

    public InstrumentData getInstrumentData() throws CommunicationException {
        String instrumentData = qdxConnector.getInstrumentData();
        try {
            return MAPPER.readValue(qdxCryptService.verify(instrumentData), InstrumentData.class);
        } catch (CryptServiceException | IOException e) {
            throw new IllegalStateException("Error getting instrument data", e);
        }
    }

    public SpotData getSpotData() throws CommunicationException {
        String spotData = qdxConnector.getSpotData();
        try {
            return MAPPER.readValue(qdxCryptService.verify(spotData), SpotData.class);
        } catch (CryptServiceException | IOException e) {
            throw new IllegalStateException("Error getting spot data", e);
        }
    }

    private JsonNode sendEncryptedMessageGetDecryptedResponse(Supplier<JsonNode> jsonCreator) throws CommunicationException {
        checkState(initialized, "Endpoint not initialized - call initialize");
        String encryptedResponse;
        try {
            Future<String> result = messageSendingExecutor.submit(() -> {
                try {
                    JsonNode message = jsonCreator.get(); // this is to get nonces in the right order
                    String encryptedMessage = qdxCryptService.encrypt(message.toString());
                    return qdxConnector.send(encryptedMessage);
                } catch (CryptServiceException e) {
                    throw new IllegalStateException("Error encrypting", e);
                }
            });
            encryptedResponse = noncancelableGet(result);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof CommunicationException) {
                throw (CommunicationException) e.getCause();
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else {
                throw new IllegalStateException("Unexpected exception", e.getCause());
            }
        }

        try {
            return MAPPER.readTree(qdxCryptService.decrypt(encryptedResponse));
        } catch (IOException | CryptServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> T noncancelableGet(Future<? extends T> result) throws ExecutionException {
        boolean interrupted = false;

        try {
            while (true) {
                try {
                    return result.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                    // fall through and retry
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
