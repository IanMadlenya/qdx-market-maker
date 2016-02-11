package com.quedex.marketmaker.qdxapi.pgp;

public class PGPEncryptionException extends PGPExceptionBase {

    public PGPEncryptionException() {
    }

    public PGPEncryptionException(String message) {
        super(message);
    }

    public PGPEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PGPEncryptionException(Throwable cause) {
        super(cause);
    }
}
