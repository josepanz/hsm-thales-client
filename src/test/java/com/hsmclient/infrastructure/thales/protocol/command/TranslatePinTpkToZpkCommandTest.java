package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.Pan;
import com.hsmclient.domain.model.PinBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comando CA (Translate PIN from TPK to ZPK). Los valores de este test son los mismos
 * vectores de ThalesAdapterTest#translatePinblkFromTpkToZpk16/32 del cliente original,
 * para no perder la unica evidencia real que existia de "esto funciono contra un HSM".
 */
class TranslatePinTpkToZpkCommandTest {

    private static final String SOURCE_TPK = "8699395CD2688A2CEFD97DAEF3BD4D61";
    private static final String SOURCE_PINBLOCK = "8EF09CD0A28D7DB0";
    private static final String PAN = "976000000001";

    @Test
    void encodesSingleLengthDestinationKeyWithoutSchemeFlag() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK),
                EncryptedKey.of("D3A8A50F20BD9B00"), // 16 hex -> simple longitud, sin flag
                PinBlock.iso0(SOURCE_PINBLOCK),
                new Pan(PAN));

        assertThat(command.requestCode()).isEqualTo("CA");
        assertThat(command.encodeBody()).isEqualTo(
                "U" + SOURCE_TPK + "D3A8A50F20BD9B00" + "12" + SOURCE_PINBLOCK + "01" + "01" + PAN);
    }

    @Test
    void encodesDoubleLengthDestinationKeyWithExplicitSchemeFlag() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK),
                EncryptedKey.of("063A0E7C0F2124E54BFA531C4095771D"), // 32 hex -> doble longitud, flag "U"
                PinBlock.iso0(SOURCE_PINBLOCK),
                new Pan(PAN));

        assertThat(command.encodeBody()).isEqualTo(
                "U" + SOURCE_TPK + "U" + "063A0E7C0F2124E54BFA531C4095771D" + "12" + SOURCE_PINBLOCK + "01" + "01" + PAN);
    }

    @Test
    void parsesSuccessfulResponseIntoDestinationPinBlock() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("D3A8A50F20BD9B00"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan(PAN));

        String remaining = "16" + "AAAABBBBCCCCDDDD" + "01"; // pin-length + destination-pinblk + format
        PinBlock result = command.parseResponse("CB", "00", remaining);

        assertThat(result.hexValue()).isEqualTo("AAAABBBBCCCCDDDD");
    }

    @Test
    void throwsHsmExceptionWhenErrorCodeIsNotZero() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("D3A8A50F20BD9B00"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan(PAN));

        assertThatThrownBy(() -> command.parseResponse("CB", "20", ""))
                .isInstanceOf(HsmException.class)
                .extracting(e -> ((HsmException) e).rawCode())
                .isEqualTo("20");
    }

    @Test
    void panLongerThan12DigitsIsTruncatedToLast12() {
        var command = new TranslatePinTpkToZpkCommand(
                EncryptedKey.of(SOURCE_TPK), EncryptedKey.of("D3A8A50F20BD9B00"),
                PinBlock.iso0(SOURCE_PINBLOCK), new Pan("4000001234560001234"));

        assertThat(command.encodeBody()).endsWith("234560001234");
    }
}
