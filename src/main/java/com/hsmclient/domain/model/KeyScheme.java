package com.hsmclient.domain.model;

/**
 * Esquema "Variant LMK" con el que el HSM interpreta una llave cifrada bajo su LMK.
 * No es parte de la llave: es un flag de un caracter que indica la longitud de la
 * llave para que el HSM sepa que variante de su LMK usar al descifrarla.
 * <p>
 * Distinto del esquema "Key Block" (ANSI TR-31 / ISO 20038), que no usa este flag
 * sino un header explicito con la metadata de la llave. Este cliente solo habla
 * Variant LMK, que es el modo de compatibilidad soportado tanto por payShield 9000
 * como por payShield 10000.
 */
public enum KeyScheme {
    SINGLE_LENGTH(""),
    DOUBLE_LENGTH("U"),
    TRIPLE_LENGTH("T");

    private final String flag;

    KeyScheme(String flag) {
        this.flag = flag;
    }

    public String flag() {
        return flag;
    }

    /** El flag solo se transmite para llaves de doble/triple longitud; simple longitud no lleva flag. */
    public boolean isExplicit() {
        return this != SINGLE_LENGTH;
    }

    public static KeyScheme fromKeyHexLength(int hexChars) {
        return switch (hexChars) {
            case 16 -> SINGLE_LENGTH;
            case 32 -> DOUBLE_LENGTH;
            case 48 -> TRIPLE_LENGTH;
            default -> throw new IllegalArgumentException(
                    "Longitud de llave invalida: " + hexChars + " hex chars (esperado 16, 32 o 48)");
        };
    }
}
