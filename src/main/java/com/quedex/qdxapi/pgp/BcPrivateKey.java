package com.quedex.qdxapi.pgp;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe
public final class BcPrivateKey {

    private final PGPSecretKey secretKey;
    private final PGPPrivateKey privateKey;
    private final String fingerprint;
    private final ImmutableMap<Long, PGPPrivateKey> privateKeys;

    public BcPrivateKey(String armoredKeyString) throws PGPKeyInitialisationException {
        this(armoredKeyString, "");
    }

    public BcPrivateKey(String armoredKeyString, String passphrase) throws PGPKeyInitialisationException {

        try {
            PGPSecretKeyRing secKeyRing = new PGPSecretKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(armoredKeyString.getBytes(StandardCharsets.UTF_8))),
                    new BcKeyFingerprintCalculator());

            PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider())
                    .build(passphrase.toCharArray());

            ImmutableMap.Builder<Long, PGPPrivateKey> builder = ImmutableMap.builder();

            for (Iterator iterator = secKeyRing.getSecretKeys(); iterator.hasNext(); ) {
                PGPPrivateKey privateKey = ((PGPSecretKey) iterator.next()).extractPrivateKey(decryptor);
                builder.put(privateKey.getKeyID(), privateKey);
            }

            this.secretKey = secKeyRing.getSecretKey();
            this.privateKeys = builder.build();
            this.privateKey = this.secretKey.extractPrivateKey(decryptor);

        } catch (PGPException | RuntimeException | IOException e) {
            throw new PGPKeyInitialisationException("Error instantiating a private key", e);
        }
        checkNotNull(this.secretKey);
        checkNotNull(this.privateKey);

        this.fingerprint = Utils.hexFingerprint(secretKey.getPublicKey());
    }

    PGPSecretKey getSecretKey() {
        return secretKey;
    }

    PGPPrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    PGPPrivateKey getPrivateKeyWithId(long keyId) throws PGPKeyNotFoundException {
        if (!privateKeys.containsKey(keyId)) {
            throw new PGPKeyNotFoundException(
                    String.format("Key with id: %s not found", Long.toHexString(keyId).toUpperCase())
            );
        }
        return privateKeys.get(keyId);
    }

    public Collection<PGPPrivateKey> getPrivateKeys() {
        return privateKeys.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        BcPrivateKey that = (BcPrivateKey) o;

        return Objects.equal(this.secretKey.getPublicKey().getKeyID(), that.secretKey.getPublicKey().getKeyID());
    }

    @Override
    public int hashCode() {
        return Long.valueOf(secretKey.getPublicKey().getKeyID()).hashCode();
    }
}
