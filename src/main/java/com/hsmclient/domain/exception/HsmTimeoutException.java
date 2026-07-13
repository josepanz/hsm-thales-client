package com.hsmclient.domain.exception;

/** El HSM no respondio dentro del timeout configurado para el comando. */
public class HsmTimeoutException extends HsmTransportException {
    public HsmTimeoutException(String commandCode, long timeoutMillis) {
        super("Timeout esperando respuesta del comando '" + commandCode + "' (" + timeoutMillis + " ms)");
    }
}
