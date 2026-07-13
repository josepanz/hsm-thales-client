package com.hsmclient.domain.model;

import java.util.regex.Pattern;

/**
 * Llave (TPK, ZPK, PVK, ZMK, BDK...) tal como circula cifrada bajo el LMK del HSM.
 * El propio HSM nunca revela el valor en claro; esta clase solo transporta el
 * criptograma hexadecimal y su {@link KeyScheme} (simple/doble/triple longitud).
 */
public record EncryptedKey(String hexValue, KeyScheme scheme) {

    private static final Pattern HEX = Pattern.compile("^[0-9A-Fa-f]+$");

    public EncryptedKey {
        if (hexValue == null || !HEX.matcher(hexValue).matches()) {
            throw new IllegalArgumentException("La llave debe ser hexadecimal");
        }
        if (scheme == null) {
            throw new IllegalArgumentException("El esquema de la llave es obligatorio");
        }
    }

    public static EncryptedKey of(String hexValue) {
        int len = hexValue == null ? 0 : hexValue.length();
        return new EncryptedKey(hexValue, KeyScheme.fromKeyHexLength(len));
    }
}
