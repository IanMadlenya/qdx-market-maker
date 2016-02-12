package com.quedex.qdxapi.pgp;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Cleartext - result of decryption and signature check.
 */
public final class Cleartext {

    private final String message;
    private final String signerKeyFingerprint;

    public Cleartext(String message, String signerKeyFingerprint) {
        this.message = checkNotNull(message);
        this.signerKeyFingerprint = signerKeyFingerprint;
    }
    public String getMessage() {
        return message;
    }

    public String getSignerKeyFingerprint() {
        return signerKeyFingerprint;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("signerKeyFingerprint", signerKeyFingerprint)
                .toString();
    }
}
