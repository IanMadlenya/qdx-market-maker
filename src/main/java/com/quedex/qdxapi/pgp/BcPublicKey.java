package com.quedex.qdxapi.pgp;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe
public final class BcPublicKey {

    private final PGPPublicKey signingKey;
    private final PGPPublicKey encryptionKey;

    private final String fingerprint;
    private final String mainKeyIdentity;

    public BcPublicKey(String armoredKeyString) throws PGPKeyInitialisationException {

        try {
            PGPPublicKeyRing pubKeyRing = new PGPPublicKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(armoredKeyString.getBytes(StandardCharsets.UTF_8))),
                    new BcKeyFingerprintCalculator()
            );

            if (Iterators.size(pubKeyRing.getPublicKeys()) < 1) {
                throw new PGPKeyInitialisationException("No keys in keyring");
            }

            signingKey = pubKeyRing.getPublicKey();

            @SuppressWarnings("unchecked")
            List<PGPPublicKey> keys = Lists.newArrayList(pubKeyRing.getPublicKeys());

            if (keys.size() == 1) {
                encryptionKey = signingKey;
            } else {
                encryptionKey = keys.get(1);
            }

            if (!encryptionKey.isEncryptionKey()) {
                throw new PGPKeyInitialisationException("Error instatiating public key: sign-only key.");
            }

        } catch (RuntimeException | IOException e) {
            throw new PGPKeyInitialisationException("Error instantiating a public key", e);
        }
        checkNotNull(signingKey);
        checkNotNull(encryptionKey);

        fingerprint = Utils.hexFingerprint(signingKey);
        mainKeyIdentity = (String) signingKey.getUserIDs().next();
    }

    PGPPublicKey getSigningKey() {
        return signingKey;
    }

    PGPPublicKey getEncryptionKey() {
        return encryptionKey;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getMainKeyIdentity() {
        return mainKeyIdentity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        BcPublicKey that = (BcPublicKey) o;

        return this.fingerprint.equals(that.fingerprint);
    }

    @Override
    public int hashCode() {
        return fingerprint.hashCode();
    }
}
