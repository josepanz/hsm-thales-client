package com.hsmclient.domain.model;

/** Offset de PIN (algoritmo IBM 3624), tipicamente 4 a 12 digitos, right-padded con 'F'. */
public record PinOffset(String value) {

    public PinOffset {
        if (value == null || !value.matches("^[0-9F]{1,12}$")) {
            throw new IllegalArgumentException("El offset de PIN debe ser numerico (relleno con 'F'), maximo 12 caracteres");
        }
    }

    /** El wire format del comando siempre ocupa 12 caracteres, relleno a la derecha con 'F'. */
    public String padded() {
        return value + "F".repeat(12 - value.length());
    }
}
