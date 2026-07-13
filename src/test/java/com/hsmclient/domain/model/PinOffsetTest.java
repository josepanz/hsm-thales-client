package com.hsmclient.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PinOffsetTest {

    @Test
    void paddedFillsUpTo12CharsWithF() {
        PinOffset offset = new PinOffset("1750");
        assertThat(offset.padded()).isEqualTo("1750FFFFFFFF");
        assertThat(offset.padded()).hasSize(12);
    }

    @Test
    void alreadyFullLengthOffsetIsUnchanged() {
        PinOffset offset = new PinOffset("123456789012");
        assertThat(offset.padded()).isEqualTo("123456789012");
    }

    @Test
    void rejectsInvalidCharacters() {
        assertThatThrownBy(() -> new PinOffset("12AB"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
