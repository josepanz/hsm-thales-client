package com.hsmclient.infrastructure.thales.protocol.codec;

/**
 * Helpers de codificacion de campos de ancho fijo del protocolo Thales.
 * <p>
 * A diferencia del cliente original (que dependia de esquemas XML sueltos, sin
 * validacion de longitud declarada vs. real - la causa del bug detectado en
 * hsm-EA32.xml, donde "zpk-type" aparecia declarado dos veces por error de copy/paste
 * en vez de "pvk-type"), aca cada campo se valida en tiempo de codificacion: si un
 * valor no calza con la longitud esperada por el comando, se falla rapido con un
 * mensaje claro en vez de mandar un mensaje corrupto al HSM.
 */
public final class FieldCodec {

    private FieldCodec() {
    }

    /** Valida que el valor tenga exactamente la longitud esperada por el campo del protocolo. */
    public static String fixed(String value, int expectedLength, String fieldName) {
        if (value == null || value.length() != expectedLength) {
            throw new IllegalArgumentException(
                    "Campo '" + fieldName + "' debe tener exactamente " + expectedLength +
                            " caracteres, recibido: " + (value == null ? "null" : value.length()));
        }
        return value;
    }

    /** Numerico con relleno de ceros a la izquierda (ej. longitud de llave RSA, indices). */
    public static String numeric(long value, int length) {
        String s = Long.toString(value);
        if (s.length() > length) {
            throw new IllegalArgumentException("Valor " + value + " no entra en " + length + " digitos");
        }
        return "0".repeat(length - s.length()) + s;
    }
}
