package com.hsmclient.domain.model;

/** Tabla de 16 hex digits usada para convertir el resultado de cifrar PAN+PIN a decimal (offset PIN). */
public record DecimalizationTable(String hexValue) {

    public DecimalizationTable {
        if (hexValue == null || !hexValue.matches("^[0-9A-Fa-f]{16}$")) {
            throw new IllegalArgumentException("La tabla de decimalizacion debe ser hexadecimal de 16 caracteres");
        }
    }
}
