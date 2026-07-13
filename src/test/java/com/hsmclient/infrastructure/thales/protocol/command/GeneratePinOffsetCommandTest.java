package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratePinOffsetCommandTest {

    private static final EncryptedKey ZPK = EncryptedKey.of("AABBCCDDEEFF00112233445566778899");
    private static final EncryptedKey PVK16 = EncryptedKey.of("54E262D26A1F1C32");
    private static final PinBlock PINBLOCK = PinBlock.iso0("EB10B1CD37EE75E2");
    private static final Pan PAN = new Pan("002008986420");
    private static final DecimalizationTable TABLE = new DecimalizationTable("FD64FC3FAD504BB3");
    private static final PinVerificationData VALIDATION_DATA = new PinVerificationData("0020089N0000");

    @Test
    void encodesPinblockKeyTypeAsZpkFixedConstant() {
        var command = new GeneratePinOffsetCommand(ZPK, PVK16, PINBLOCK, PAN, TABLE, VALIDATION_DATA);

        assertThat(command.requestCode()).isEqualTo("BK");
        assertThat(command.encodeBody()).isEqualTo(
                "001" + "U" + ZPK.hexValue() + PVK16.hexValue() + PINBLOCK.hexValue()
                        + "01" + "04" + PAN.last12() + TABLE.hexValue() + VALIDATION_DATA.value());
    }

    @Test
    void parsesTheGeneratedOffsetFromTheResponse() {
        var command = new GeneratePinOffsetCommand(ZPK, PVK16, PINBLOCK, PAN, TABLE, VALIDATION_DATA);

        PinOffset offset = command.parseResponse("BL", "00", "1750FFFFFFFF");

        assertThat(offset.value()).isEqualTo("1750FFFFFFFF");
    }
}
