package net.quedex.api;

public class CryptServiceException extends Exception {

    public CryptServiceException() {
    }

    public CryptServiceException(String message) {
        super(message);
    }

    public CryptServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptServiceException(Throwable cause) {
        super(cause);
    }
}
