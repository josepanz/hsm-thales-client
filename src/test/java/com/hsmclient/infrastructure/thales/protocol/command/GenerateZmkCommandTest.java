package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.GeneratedKey;
import com.hsmclient.domain.model.KeyScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateZmkCommandTest {

    @Test
    void underLmkEncodesMode0WithoutWrappingKey() {
        var command = GenerateZmkCommand.underLmk(KeyScheme.DOUBLE_LENGTH);

        assertThat(command.requestCode()).isEqualTo("A0");
        assertThat(command.encodeBody()).isEqualTo("0" + "000" + "U");
    }

    @Test
    void underLmkParsesKeyAndCheckValue() {
        var command = GenerateZmkCommand.underLmk(KeyScheme.DOUBLE_LENGTH);
        String key32 = "AAAABBBBCCCCDDDDEEEEFFFF00001111";
        String checkValue = "12CD34";

        GeneratedKey result = command.parseResponse("A1", "00", key32 + checkValue);

        assertThat(result.key().hexValue()).isEqualTo(key32);
        assertThat(result.checkValue()).isEqualTo(checkValue);
    }

    @Test
    void underZmkEncodesMode1WithWrappingKey() {
        EncryptedKey wrappingZmk = EncryptedKey.of("11112222333344445555666677778888");
        var command = GenerateZmkCommand.underZmk(wrappingZmk);

        assertThat(command.encodeBody()).isEqualTo("1" + "001" + "U" + wrappingZmk.hexValue() + "U");
    }
}
