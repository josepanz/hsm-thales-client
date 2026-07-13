package com.hsmclient.infrastructure.luna;

import com.hsmclient.domain.exception.HsmTransportException;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.Signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A diferencia de LunaCryptoAdapter, esto SI se puede probar de punta a punta sin hardware:
 * es JCE puro contra los providers por defecto de la JVM. Sirve como especificacion viva del
 * contrato de GenericCryptoPort mientras no haya un Luna real para probar contra el.
 */
class MockGenericCryptoAdapterTest {

    @Test
    void generateKeyPairReturnsAKeyOfTheRequestedSize() {
        MockGenericCryptoAdapter adapter = new MockGenericCryptoAdapter();

        KeyPair pair = adapter.generateKeyPair("app-signing-key", "RSA", 2048);

        assertThat(pair.getPublic()).isNotNull();
        assertThat(pair.getPrivate()).isNotNull();
    }

    @Test
    void signatureCanBeVerifiedWithThePublicKeyOfTheSameAlias() throws Exception {
        MockGenericCryptoAdapter adapter = new MockGenericCryptoAdapter();
        KeyPair pair = adapter.generateKeyPair("app-signing-key", "RSA", 2048);
        byte[] data = "hola mundo".getBytes();

        byte[] signatureBytes = adapter.sign("app-signing-key", data, "SHA256withRSA");

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(pair.getPublic());
        verifier.update(data);
        assertThat(verifier.verify(signatureBytes)).isTrue();
    }

    @Test
    void signingWithAnUnknownAliasFailsClearlyInsteadOfNpe() {
        MockGenericCryptoAdapter adapter = new MockGenericCryptoAdapter();

        assertThatThrownBy(() -> adapter.sign("no-existe", "data".getBytes(), "SHA256withRSA"))
                .isInstanceOf(HsmTransportException.class)
                .hasMessageContaining("no-existe");
    }

    @Test
    void wrappedKeyCanBeUnwrappedWithThePrivateKeyOfTheSameAlias() throws Exception {
        MockGenericCryptoAdapter adapter = new MockGenericCryptoAdapter();
        KeyPair pair = adapter.generateKeyPair("wrapping-key", "RSA", 2048);
        byte[] rawAesKey = "0123456789ABCDEF".getBytes(); // 16 bytes -> AES-128

        byte[] wrapped = adapter.wrapKey("wrapping-key", rawAesKey, "RSA/ECB/PKCS1Padding");

        Cipher unwrapper = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        unwrapper.init(Cipher.UNWRAP_MODE, pair.getPrivate());
        SecretKey unwrapped = (SecretKey) unwrapper.unwrap(wrapped, "AES", Cipher.SECRET_KEY);

        assertThat(unwrapped.getEncoded()).isEqualTo(rawAesKey);
    }
}
