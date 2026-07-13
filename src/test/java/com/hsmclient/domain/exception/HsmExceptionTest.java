package com.hsmclient.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HsmExceptionTest {

    @Test
    void carriesBothRawCodeAndResolvedErrorCode() {
        HsmException e = new HsmException("14");
        assertThat(e.rawCode()).isEqualTo("14");
        assertThat(e.errorCode()).isEqualTo(ThalesErrorCode.PIN_UNDER_LMK_02_03_INVALID);
        assertThat(e.getMessage()).isEqualTo(ThalesErrorCode.PIN_UNDER_LMK_02_03_INVALID.description());
    }
}
