package com.hsmclient.infrastructure.thales.protocol;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.exception.ThalesErrorCode;

public final class ThalesResponses {

    private static final String SUCCESS = "00";

    private ThalesResponses() {
    }

    /** Todas las respuestas Thales traen el error code justo despues del response code; si no es 00, no hay mas campos que parsear. */
    public static void requireSuccess(String errorCode) {
        if (!SUCCESS.equals(errorCode)) {
            throw new HsmException(errorCode);
        }
    }

    public static boolean isSuccess(String errorCode) {
        return ThalesErrorCode.fromCode(errorCode).isSuccess();
    }
}
