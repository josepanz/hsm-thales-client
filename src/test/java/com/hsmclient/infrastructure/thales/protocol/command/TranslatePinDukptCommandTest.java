package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.Ksn;
import com.hsmclient.domain.model.Pan;
import com.hsmclient.domain.model.PinBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Comando G0, vectores tomados de ThalesManagerTest#testTranslateDUKPT32 del cliente original. */
class TranslatePinDukptCommandTest {

    @Test
    void encodesBdkKsnAndPinBlockInTheExpectedOrder() {
        String sourceKey = "E56448F66FDC9B57ECB87469CB998830";
        String destinationKey = "DEF7DBE03A6BF1FB2EB41B5F57892F07";
        String pinblock = "8A56466ADDCAEA1B";
        String pan = "625960050010";

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
                EncryptedKey.of("E56448F66FDC9B57ECB87469CB998830"),
                EncryptedKey.of("DEF7DBE03A6BF1FB2EB41B5F57892F07"),
                new Ksn("609", "FFFF0000100000200674"),
                PinBlock.iso0("8A56466ADDCAEA1B"),
                new Pan("625960050010"));

        String remaining = "16" + "1122334455667788"; // destination-pinblk-length + destination-pinblk
        PinBlock result = command.parseResponse("G1", "00", remaining);

        assertThat(result.hexValue()).isEqualTo("1122334455667788");
    }
}
