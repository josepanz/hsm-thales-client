package com.hsmclient.domain.exception;

/** Fallo de infraestructura (conexion caida, socket error, sin nodos disponibles) hablando con el HSM. */
public class HsmTransportException extends RuntimeException {
    public HsmTransportException(String message) {
        super(message);
    }

    public HsmTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
