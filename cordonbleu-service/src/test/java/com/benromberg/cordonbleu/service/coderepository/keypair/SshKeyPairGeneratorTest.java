package com.benromberg.cordonbleu.service.coderepository.keypair;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshKeyPairGenerator;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Signature;

public class SshKeyPairGeneratorTest {
    private static final String SIGNATURE_CONTENT = "sign";
    private static final String SSH_PASSWORD = "ssh password";

    @Test
    public void privateKey_CanBeDecrypted() throws Exception {
        com.benromberg.cordonbleu.data.util.KeyPair keyPair = new SshKeyPairGenerator(() -> SSH_PASSWORD).generate();
        KeyPair jSchKeyPair = KeyPair.load(new JSch(), keyPair.getPrivateKey().getBytes(), keyPair.getPublicKey()
                .getBytes());
        assertThat(jSchKeyPair.decrypt(SSH_PASSWORD)).isTrue();
    }

    @Test
    public void keys_CanVerifySignature() throws Exception {
        com.benromberg.cordonbleu.data.util.KeyPair keyPair = new SshKeyPairGenerator(() -> SSH_PASSWORD).generate();
        KeyPair signingKeyPair = KeyPair.load(new JSch(), keyPair.getPrivateKey().getBytes(), null);
        signingKeyPair.decrypt(SSH_PASSWORD);
        byte[] signature = signingKeyPair.getSignature(SIGNATURE_CONTENT.getBytes());
        KeyPair verifyingKeyPair = KeyPair.load(new JSch(), null, keyPair.getPublicKey().getBytes());
        Signature verifier = verifyingKeyPair.getVerifier();
        verifier.update(SIGNATURE_CONTENT.getBytes());
        assertThat(verifier.verify(signature)).isTrue();
    }

    @Test
    public void publicKey_HasNoComment() throws Exception {
        com.benromberg.cordonbleu.data.util.KeyPair keyPair = new SshKeyPairGenerator(() -> SSH_PASSWORD).generate();
        assertThat(keyPair.getPublicKey().chars().filter(character -> character == ' ').count()).isEqualTo(1);
    }

    @Test
    public void publicKey_HasNoTrailingLinebreak() throws Exception {
        com.benromberg.cordonbleu.data.util.KeyPair keyPair = new SshKeyPairGenerator(() -> SSH_PASSWORD).generate();
        assertThat(keyPair.getPublicKey().trim()).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    public void privateKey_HasNoTrailingLinebreak() throws Exception {
        com.benromberg.cordonbleu.data.util.KeyPair keyPair = new SshKeyPairGenerator(() -> SSH_PASSWORD).generate();
        assertThat(keyPair.getPrivateKey().trim()).isEqualTo(keyPair.getPrivateKey());
    }
}
