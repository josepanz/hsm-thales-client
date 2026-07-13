package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comando EA. Cubre especificamente la correccion del bug de hsm-EA32.xml: el flag de
 * esquema del PVK debe salir del esquema del PVK (no ser una copia pegada del flag del ZPK).
 */
class ValidatePinOffsetCommandTest {

    private static final EncryptedKey ZPK = EncryptedKey.of("AABBCCDDEEFF00112233445566778899"); // 32 hex
    private static final PinBlock PINBLOCK = PinBlock.iso0("EB10B1CD37EE75E2");
    private static final Pan PAN = new Pan("002008986420");
    private static final DecimalizationTable TABLE = new DecimalizationTable("FD64FC3FAD504BB3");
    private static final PinVerificationData VALIDATION_DATA = new PinVerificationData("0020089N0000");
    private static final PinOffset OFFSET = new PinOffset("1750");

    @Test
    void singleLengthPvkHasNoSchemeFlag() {
        EncryptedKey pvk16 = EncryptedKey.of("54E262D26A1F1C32"); // 16 hex -> simple longitud
        var command = new ValidatePinOffsetCommand(ZPK, pvk16, PINBLOCK, PAN, TABLE, VALIDATION_DATA, OFFSET);

        String body = command.encodeBody();
        assertThat(body).startsWith("U" + ZPK.hexValue());
        assertThat(body).contains(pvk16.hexValue());
        // el flag "U" del zpk esta una sola vez; no debe repetirse pegado al pvk simple longitud
        assertThat(body).isEqualTo(
                "U" + ZPK.hexValue() + pvk16.hexValue() + "12" + PINBLOCK.hexValue() + "01" + "04"
                        + PAN.last12() + TABLE.hexValue() + VALIDATION_DATA.value() + OFFSET.padded());
    }

    @Test
    void doubleLengthPvkGetsItsOwnSchemeFlagNotACopyOfTheZpkFlag() {
        EncryptedKey pvk32 = EncryptedKey.of("54E262D26A1F1C3254E262D26A1F1C32"); // 32 hex -> doble longitud
        var command = new ValidatePinOffsetCommand(ZPK, pvk32, PINBLOCK, PAN, TABLE, VALIDATION_DATA, OFFSET);

        assertThat(command.encodeBody()).isEqualTo(
                "U" + ZPK.hexValue() + "U" + pvk32.hexValue() + "12" + PINBLOCK.hexValue() + "01" + "04"
                        + PAN.last12() + TABLE.hexValue() + VALIDATION_DATA.value() + OFFSET.padded());
    }

    @Test
    void successfulValidationReturnsNormally() {
        EncryptedKey pvk16 = EncryptedKey.of("54E262D26A1F1C32");
        var command = new ValidatePinOffsetCommand(ZPK, pvk16, PINBLOCK, PAN, TABLE, VALIDATION_DATA, OFFSET);

        assertThat(command.parseResponse("EB", "00", "")).isNull();
    }

    @Test
    void mismatchedOffsetThrowsHsmException() {
        EncryptedKey pvk16 = EncryptedKey.of("54E262D26A1F1C32");
        var command = new ValidatePinOffsetCommand(ZPK, pvk16, PINBLOCK, PAN, TABLE, VALIDATION_DATA, OFFSET);

        assertThatThrownBy(() -> command.parseResponse("EB", "01", ""))
                .isInstanceOf(HsmException.class);
    }
}
