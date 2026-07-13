package com.hsmclient.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PinBlockTest {

    @Test
    void iso0FactoryUsesIso0Format() {
        PinBlock block = PinBlock.iso0("8EF09CD0A28D7DB0");
        assertThat(block.format()).isEqualTo(PinBlockFormat.ISO_0);
    }

    @Test
    void rejectsWrongLength() {
        assertThatThrownBy(() -> PinBlock.iso0("ABCD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonHex() {
        assertThatThrownBy(() -> PinBlock.iso0("ZZZZZZZZZZZZZZZZ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
