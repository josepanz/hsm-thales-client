package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.Pan;
import com.hsmclient.domain.model.PinBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Comando CA (Translate PIN from TPK to ZPK). Vectores sinteticos, sin valor criptografico real. */
class TranslatePinTpkToZpkCommandTest {

    private static final String SOURCE_TPK = "0123456789ABCDEF0123456789ABCDEF";
    private static final String SOURCE_PINBLOCK = "FEDCBA9876543210";
    private static final String PAN = "444455556666";

    @Test
    void encodesSingleLengthDestinationKeyWithoutSchemeFlag() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK),
                EncryptedKey.of("1122334455667788"), // 16 hex -> simple longitud, sin flag
                PinBlock.iso0(SOURCE_PINBLOCK),
                new Pan(PAN));

        assertThat(command.requestCode()).isEqualTo("CA");
        assertThat(command.encodeBody()).isEqualTo(
                "U" + SOURCE_TPK + "1122334455667788" + "12" + SOURCE_PINBLOCK + "01" + "01" + PAN);
    }

    @Test
    void encodesDoubleLengthDestinationKeyWithExplicitSchemeFlag() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK),
                EncryptedKey.of("1122334455667788AABBCCDDEEFF0011"), // 32 hex -> doble longitud, flag "U"
                PinBlock.iso0(SOURCE_PINBLOCK),
                new Pan(PAN));

        assertThat(command.encodeBody()).isEqualTo(
                "U" + SOURCE_TPK + "U" + "1122334455667788AABBCCDDEEFF0011" + "12" + SOURCE_PINBLOCK + "01" + "01" + PAN);
    }

    @Test
    void parsesSuccessfulResponseIntoDestinationPinBlock() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("1122334455667788"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan(PAN));

        String remaining = "16" + "0F1E2D3C4B5A6978" + "01"; // pin-length + destination-pinblk + format
        PinBlock result = command.parseResponse("CB", "00", remaining);

        assertThat(result.hexValue()).isEqualTo("0F1E2D3C4B5A6978");
    }

    @Test
    void throwsHsmExceptionWhenErrorCodeIsNotZero() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("1122334455667788"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan(PAN));

        assertThatThrownBy(() -> command.parseResponse("CB", "20", ""))
                .isInstanceOf(HsmException.class)
                .extracting(e -> ((HsmException) e).rawCode())
                .isEqualTo("20");
    }

    @Test
    void panLongerThan12DigitsIsTruncatedToLast12() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("1122334455667788"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan("555512340000123456"));

        assertThat(command.encodeBody()).endsWith("340000123456");
    }
}
