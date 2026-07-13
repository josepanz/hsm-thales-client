package com.hsmclient.domain.exception;

/** Error de negocio devuelto por el HSM (respuesta con codigo distinto de 00). */
public class HsmException extends RuntimeException {

    private final ThalesErrorCode errorCode;
    private final String rawCode;

    public HsmException(String rawCode) {
        super(ThalesErrorCode.fromCode(rawCode).description());
        this.errorCode = ThalesErrorCode.fromCode(rawCode);
        this.rawCode = rawCode;
    }

    public ThalesErrorCode errorCode() {
        return errorCode;
    }

    public String rawCode() {
        return rawCode;
    }

    @Override
    public String toString() {
        return "HsmException{code='" + rawCode + "', error=" + errorCode + "}";
    }
}
