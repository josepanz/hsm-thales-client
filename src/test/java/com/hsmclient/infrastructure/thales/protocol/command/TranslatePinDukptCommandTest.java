package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.Ksn;
import com.hsmclient.domain.model.Pan;
import com.hsmclient.domain.model.PinBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Comando G0. Vectores sinteticos, sin valor criptografico real. */
class TranslatePinDukptCommandTest {

    @Test
    void encodesBdkKsnAndPinBlockInTheExpectedOrder() {
        String sourceKey = "AABBCCDDEEFF00112233445566778899";
        String destinationKey = "998877665544332211FFEEDDCCBBAA00";
        String pinblock = "1357924680ACE0BD";
        String pan = "700099998888";

        var command = new TranslatePinDukptCommand(
                EncryptedKey.of(sourceKey),
                EncryptedKey.of(destinationKey),
                new Ksn("609", "FFFF0000100000200674"),
                PinBlock.iso0(pinblock),
                new Pan(pan));

        assertThat(command.requestCode()).isEqualTo("G0");
        assertThat(command.encodeBody()).isEqualTo(
                "~" + "U" + sourceKey + "U" + destinationKey + "609" + "FFFF0000100000200674"
                        + pinblock + "01" + "01" + pan);
    }

    @Test
    void parsesDestinationPinBlockFromResponse() {
        var command = new TranslatePinDukptCommand(
                EncryptedKey.of("AABBCCDDEEFF00112233445566778899"),
                EncryptedKey.of("998877665544332211FFEEDDCCBBAA00"),
                new Ksn("609", "FFFF0000100000200674"),
                PinBlock.iso0("1357924680ACE0BD"),
                new Pan("700099998888"));

        String remaining = "16" + "AABBCCDDEEFF0011"; // destination-pinblk-length + destination-pinblk
        PinBlock result = command.parseResponse("G1", "00", remaining);

        assertThat(result.hexValue()).isEqualTo("AABBCCDDEEFF0011");
    }
}
