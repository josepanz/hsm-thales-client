package com.hsmclient.domain.model;

import java.util.regex.Pattern;

/** PIN block cifrado (16 hex chars / 8 bytes), nunca contiene el PIN en claro. */
public record PinBlock(String hexValue, PinBlockFormat format) {

    private static final Pattern HEX_16 = Pattern.compile("^[0-9A-Fa-f]{16}$");

    public PinBlock {
        if (hexValue == null || !HEX_16.matcher(hexValue).matches()) {
            throw new IllegalArgumentException("El PIN block debe ser hexadecimal de 16 caracteres");
        }
        if (format == null) {
            throw new IllegalArgumentException("El formato del PIN block es obligatorio");
        }
    }

    public static PinBlock iso0(String hexValue) {
        return new PinBlock(hexValue, PinBlockFormat.ISO_0);
    }
}
