package com.hsmclient.infrastructure.thales.protocol.codec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Este es exactamente el tipo de validacion que evita bugs como un campo declarado con
 * el nombre equivocado (ver `hsm-EA32.xml` en `docs/SECURITY_FINDINGS.md`) que nadie
 * detecta porque no hay chequeo de longitud/tipo en tiempo de armado del mensaje.
 */
class FieldCodecTest {

    @Test
    void fixedAcceptsExactLength() {
        assertThat(FieldCodec.fixed("ABCD", 4, "campo")).isEqualTo("ABCD");
    }

    @Test
    void fixedRejectsWrongLengthWithClearMessage() {
        assertThatThrownBy(() -> FieldCodec.fixed("ABC", 4, "pvk"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pvk")
                .hasMessageContaining("4");
    }

    @Test
    void numericPadsWithLeadingZeros() {
        assertThat(FieldCodec.numeric(48, 4)).isEqualTo("0048");
    }

    @Test
    void numericRejectsValueThatDoesNotFit() {
        assertThatThrownBy(() -> FieldCodec.numeric(123456, 4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
