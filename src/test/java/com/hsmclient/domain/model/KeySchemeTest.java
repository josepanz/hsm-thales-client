package com.hsmclient.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El flag "U"/"T" es la unica pieza del protocolo que distingue simple/doble/triple
 * longitud; si esta logica esta mal, todos los comandos que envuelven llaves quedan mal.
 */
class KeySchemeTest {

    @Test
    @DisplayName("Dado 16 hex chars (8 bytes), el esquema es simple longitud sin flag explicito")
    void singleLength() {
        assertThat(KeyScheme.fromKeyHexLength(16)).isEqualTo(KeyScheme.SINGLE_LENGTH);
        assertThat(KeyScheme.SINGLE_LENGTH.isExplicit()).isFalse();
        assertThat(KeyScheme.SINGLE_LENGTH.flag()).isEmpty();
    }

    @Test
    @DisplayName("Dado 32 hex chars (16 bytes), el esquema es doble longitud con flag 'U'")
    void doubleLength() {
        assertThat(KeyScheme.fromKeyHexLength(32)).isEqualTo(KeyScheme.DOUBLE_LENGTH);
        assertThat(KeyScheme.DOUBLE_LENGTH.isExplicit()).isTrue();
        assertThat(KeyScheme.DOUBLE_LENGTH.flag()).isEqualTo("U");
    }

    @Test
    @DisplayName("Dado 48 hex chars (24 bytes), el esquema es triple longitud con flag 'T'")
    void tripleLength() {
        assertThat(KeyScheme.fromKeyHexLength(48)).isEqualTo(KeyScheme.TRIPLE_LENGTH);
        assertThat(KeyScheme.TRIPLE_LENGTH.flag()).isEqualTo("T");
    }

    @Test
    @DisplayName("Una longitud que no es 16/32/48 hex chars es invalida para cualquier llave DES")
    void invalidLength() {
        assertThatThrownBy(() -> KeyScheme.fromKeyHexLength(20))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
