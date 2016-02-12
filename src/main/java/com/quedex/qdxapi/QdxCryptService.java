package com.quedex.qdxapi;

import com.quedex.qdxapi.pgp.*;
import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;

/**
 * Encryption, decryption and verification functionality for a single account.
 *
 * <p>{@link #encrypt(String)} is not thread safe, other methods are.
 */
public class QdxCryptService {

    private final BcEncryptor encryptorToQuedex;
    private final BcDecryptor decryptorFromQuedex;
    private final BcSignatureVerifier signatureVerifierFromQuedex;

    public QdxCryptService(BcPrivateKey accountPrivateKey, BcPublicKey quedexPublicKey) {
        encryptorToQuedex = new BcEncryptor(quedexPublicKey, accountPrivateKey);
        decryptorFromQuedex = new BcDecryptor(quedexPublicKey, accountPrivateKey);
        signatureVerifierFromQuedex = new BcSignatureVerifier(quedexPublicKey);
    }

    public String encrypt(String message) throws CryptServiceException {
        try {
            return encryptorToQuedex.encrypt(message);
        } catch (PGPKeyNotFoundException e) {
            throw new AssertionError("Cannot happen, we have the key", e);
        } catch (PGPEncryptionException e) {
            throw new CryptServiceException("Error encrypting", e);
        }
    }

    public String decrypt(String ciphertext) throws CryptServiceException {
        try {
            Cleartext result = decryptorFromQuedex.decrypt(ciphertext);
            return result.getMessage();
        } catch (PGPDecryptionException | PGPInvalidSignatureException e) {
            throw new CryptServiceException(e);
        } catch (PGPUnknownRecipientException e) {
            throw new CryptServiceException("Message encrypted for someone else", e);
        }catch (PGPKeyNotFoundException e) {
            throw new CryptServiceException("Message not encrypted by Quedex", e);
        }
    }

    public String verify(String signedCleartext) throws CryptServiceException {
        try {
            Cleartext result = signatureVerifierFromQuedex.verifySignature(signedCleartext);
            return result.getMessage();
        } catch (IOException | PGPException | PGPInvalidSignatureException e) {
            throw new CryptServiceException(e);
        } catch (PGPKeyNotFoundException e) {
            throw new CryptServiceException("Message not encrypted by Quedex", e);
        }
    }
}
