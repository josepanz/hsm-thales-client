package com.hsmclient.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThalesErrorCodeTest {

    @Test
    void code00IsSuccess() {
        assertThat(ThalesErrorCode.fromCode("00").isSuccess()).isTrue();
    }

    @Test
    void knownErrorCodeResolvesToItsDescription() {
        assertThat(ThalesErrorCode.fromCode("15").description())
                .isEqualTo("Datos de entrada invalidos (formato, caracteres o datos insuficientes)");
    }

    @Test
    void unknownCodeFallsBackToUnknownInsteadOfThrowing() {
        assertThat(ThalesErrorCode.fromCode("ZZ")).isEqualTo(ThalesErrorCode.UNKNOWN);
    }
}
