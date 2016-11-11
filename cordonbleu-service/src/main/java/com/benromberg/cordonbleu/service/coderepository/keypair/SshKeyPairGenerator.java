package com.benromberg.cordonbleu.service.coderepository.keypair;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import com.benromberg.cordonbleu.data.util.KeyPairGenerator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

public class SshKeyPairGenerator implements KeyPairGenerator {
    private static final int KEY_SIZE = 2048;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final String password;

    @Inject
    public SshKeyPairGenerator(SshPrivateKeyPasswordProvider passwordProvider) {
        password = passwordProvider.getSshPrivateKeyPassword();
    }

    @Override
    public com.benromberg.cordonbleu.data.util.KeyPair generate() {
        return convertException(() -> {
            KeyPair keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, KEY_SIZE);
            String publicKey = readKey(output -> keyPair.writePublicKey(output, ""));
            String privateKey = readKey(output -> keyPair.writePrivateKey(output, password.getBytes(CHARSET)));
            return new com.benromberg.cordonbleu.data.util.KeyPair(privateKey, publicKey);
        });
    }

    private String readKey(Consumer<OutputStream> writer) throws UnsupportedEncodingException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.accept(output);
        return output.toString(CHARSET.toString()).trim();
    }
}
