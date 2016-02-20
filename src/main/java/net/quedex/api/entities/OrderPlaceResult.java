package net.quedex.api.entities;

import com.google.common.base.MoreObjects;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
public final class OrderPlaceResult {

    private final boolean success;
    private final Optional<Long> systemOrderId; // id given by the system
    private final String errorMessage;          // empty if no error

    public OrderPlaceResult(long systemOrderId) {
        this.success = true;
        this.systemOrderId = Optional.of(systemOrderId);
        this.errorMessage = "";
    }

    public OrderPlaceResult(String errorMessage) {
        this.success = false;
        this.systemOrderId = Optional.empty();
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<Long> getSystemOrderId() {
        return systemOrderId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("systemOrderId", systemOrderId)
                .add("errorMessage", errorMessage)
                .toString();
    }
}
