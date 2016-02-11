package com.quedex.marketmaker.qdxapi.pgp;

import org.bouncycastle.openpgp.PGPPublicKey;

public final class Utils {
    private Utils() { throw new AssertionError(); }

    public static String hexFingerprint(PGPPublicKey publicKey) {
        byte[] bytes = publicKey.getFingerprint();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
