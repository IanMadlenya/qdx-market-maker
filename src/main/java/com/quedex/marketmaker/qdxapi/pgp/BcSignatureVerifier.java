package com.quedex.marketmaker.qdxapi.pgp;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public final class BcSignatureVerifier {

    private final BcPublicKey publicKey;

    public BcSignatureVerifier(BcPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public Cleartext verifySignature(String message)
            throws IOException, PGPKeyNotFoundException, PGPException, PGPInvalidSignatureException {
        ArmoredInputStream armoredInputStream = new ArmoredInputStream(new ByteArrayInputStream(message.getBytes()));

        ByteArrayOutputStream cleartextOut = new ByteArrayOutputStream();
        int ch;

        while ((ch = armoredInputStream.read()) >= 0 && armoredInputStream.isClearText()) {
            cleartextOut.write((byte) ch);
        }

        BcPGPObjectFactory pgpFact = new BcPGPObjectFactory(armoredInputStream);
        PGPSignatureList signatureList = (PGPSignatureList) pgpFact.nextObject();
        checkState(signatureList.size() == 1);
        PGPSignature sig = signatureList.get(0);

        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey.getSigningKey());

        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        ByteArrayOutputStream fullOut = new ByteArrayOutputStream();
        InputStream sigIn = new ByteArrayInputStream(cleartextOut.toByteArray());

        boolean first_line = true;
        while (true) {
            ch = sigIn.read();

            if (ch < 0) {
                break;
            }

            if (ch == '\r') {
                ch = sigIn.read();
            }
            if (ch == '\n') {
                if (!first_line) {
                    sig.update((byte) '\r');
                    sig.update((byte) '\n');
                    fullOut.write((byte) '\r');
                    fullOut.write((byte) '\n');
                }
                first_line = false;
                byte[] line = lineOut.toByteArray();
                int length = getLengthWithoutWhiteSpace(line);
                sig.update(line, 0, length);
                lineOut.reset();
            } else {
                lineOut.write(ch);
                fullOut.write(ch);
            }
        }

        if (sig.verify()) {
            return new Cleartext(fullOut.toString(), publicKey.getFingerprint());
        }

        throw new PGPInvalidSignatureException("Wrong signature");
    }

    private static int getLengthWithoutWhiteSpace(byte[] line) {
        int end = line.length - 1;

        while (end >= 0 && isWhiteSpace(line[end])) {
            end--;
        }

        return end + 1;
    }

    private static boolean isWhiteSpace(byte b) {
        return b == '\r' || b == '\n' || b == '\t' || b == ' ';
    }
}
