package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.RsaKeyPair;
import com.hsmclient.infrastructure.thales.protocol.codec.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El request esta verificado contra el ejemplo real documentado en el recurso original
 * (hsm-EI.xml): header "000000000001" + "EI" + "0" + "2048" + "01" = "000000000001EI0204801".
 */
class GenerateRsaKeyPairCommandTest {

    @Test
    void encodesKeyLengthAndFixedFlagsMatchingTheDocumentedExample() {
        var command = new GenerateRsaKeyPairCommand(2048);

        assertThat(command.requestCode()).isEqualTo("EI");
        assertThat(command.encodeBody()).isEqualTo("0" + "2048" + "01");
        assertThat("EI" + command.encodeBody()).isEqualTo("EI0204801");
    }

    @Test
    void parsesLengthPrefixedPrivateKey() {
        var command = new GenerateRsaKeyPairCommand(2048);
        String publicKeyHex = "AAAABBBBCCCCDDDDEEEEFFFF00001111"; // 32 hex -> aunque sea 16 bytes
        assertThat(publicKeyHex).hasSize(32);
        String privateKeyBytes = "0004"; // 4 bytes
        String privateKeyHex = "DEADBEEF"; // 4 bytes = 8 hex chars

        RsaKeyPair result = command.parseResponse("EJ", "00", publicKeyHex + privateKeyBytes + privateKeyHex);

        assertThat(Hex.encode(result.publicKey())).isEqualTo(publicKeyHex);
        assertThat(Hex.encode(result.encryptedPrivateKey())).isEqualTo(privateKeyHex);
    }
}
