package com.hsmclient.infrastructure.thales.protocol;

/**
 * Un comando Thales: sabe codificar sus propios campos y parsear su propia respuesta.
 * El transporte (ThalesConnection) solo conoce esta interfaz - no sabe nada de CA, EA, etc.
 *
 * @param <R> tipo de dominio que representa el resultado (PinBlock, PinOffset, HsmHealth...).
 */
public interface ThalesCommand<R> {

    /** Codigo de 2 letras del comando (ej. "CA", "EA", "G0"). */
    String requestCode();

    /** Campos especificos del comando, sin el header ni el codigo (esos los agrega la conexion). */
    String encodeBody();

    /**
     * Parsea la respuesta ya separada en response-code/error-code/resto de campos.
     * Debe empezar validando el error code (ver {@link ThalesResponses#requireSuccess}).
     */
    R parseResponse(String responseCode, String errorCode, String remainingFields);
}
