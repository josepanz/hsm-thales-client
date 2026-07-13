package com.hsmclient.domain.model;

/** Datos de validacion de PIN (IBM 3624): PAN o dato alternativo usado junto al PVK. */
public record PinVerificationData(String value) {

    public PinVerificationData {
        if (value == null || value.length() != 12) {
            throw new IllegalArgumentException("El dato de validacion de PIN debe tener exactamente 12 caracteres");
        }
    }
}
