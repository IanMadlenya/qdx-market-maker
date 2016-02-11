package com.quedex.marketmaker.qdxapi.entities;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
public final class OrderModifyResult {

    private final boolean success;
    private final String errorMessage; // empty if no error

    public OrderModifyResult() {
        this.success = true;
        this.errorMessage = "";
    }

    public OrderModifyResult(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
