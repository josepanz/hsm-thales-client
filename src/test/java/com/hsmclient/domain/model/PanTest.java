package com.hsmclient.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Los comandos de PIN de Thales solo aceptan los ultimos 12 digitos del PAN (sin check digit). */
class PanTest {

    @Test
    void last12TruncatesLongerPans() {
        Pan pan = new Pan("4000001234560001234");
        assertThat(pan.last12()).isEqualTo("234560001234");
        assertThat(pan.last12()).hasSize(12);
    }

    @Test
    void last12KeepsShorterPansAsIs() {
        Pan pan = new Pan("976000000001");
        assertThat(pan.last12()).isEqualTo("976000000001");
    }

    @Test
    void rejectsNonNumericPan() {
        assertThatThrownBy(() -> new Pan("abc123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
