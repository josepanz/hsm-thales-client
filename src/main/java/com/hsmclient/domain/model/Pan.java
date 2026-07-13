package com.hsmclient.domain.model;

/**
 * PAN de la tarjeta. Los comandos Thales de PIN solo aceptan los ultimos 12 digitos
 * (sin el check digit), por lo que la normalizacion vive aqui en vez de repetirse
 * en cada comando.
 */
public record Pan(String value) {

    public Pan {
        if (value == null || !value.matches("^\\d{8,19}$")) {
            throw new IllegalArgumentException("PAN invalido: debe ser numerico de 8 a 19 digitos");
        }
    }

    public String last12() {
        return value.length() > 12 ? value.substring(value.length() - 12) : value;
    }
}
