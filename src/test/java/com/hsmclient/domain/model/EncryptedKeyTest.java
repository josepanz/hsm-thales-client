package com.hsmclient.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptedKeyTest {

    @Test
    void ofDerivesSchemeFromHexLength() {
        EncryptedKey doubleLength = EncryptedKey.of("8699395CD2688A2CEFD97DAEF3BD4D61".substring(0, 32));
        assertThat(doubleLength.scheme()).isEqualTo(KeyScheme.DOUBLE_LENGTH);
    }

    @Test
    void rejectsNonHexValues() {
        assertThatThrownBy(() -> EncryptedKey.of("NOT-HEX-VALUE-1234567890123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidLength() {
        assertThatThrownBy(() -> EncryptedKey.of("ABCD"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
