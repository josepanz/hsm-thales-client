package com.hsmclient.domain.model;

/**
 * Key Serial Number de un esquema DUKPT. Identifica, junto con el BDK, cual es la
 * llave de transaccion derivada que el terminal uso para cifrar el PIN block; el HSM
 * la reconstruye a partir de estos datos en vez de tener una llave fija almacenada.
 */
public record Ksn(String descriptor, String value) {

    public Ksn {
        if (descriptor == null || descriptor.isBlank()) {
            throw new IllegalArgumentException("El descriptor de KSN es obligatorio");
        }
        if (value == null || !value.matches("^[0-9A-Fa-f]+$")) {
            throw new IllegalArgumentException("El KSN debe ser hexadecimal");
        }
    }
}
