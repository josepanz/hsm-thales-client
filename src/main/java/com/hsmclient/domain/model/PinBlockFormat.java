package com.hsmclient.domain.model;

/** Formatos de PIN block ISO 9564 que el HSM entiende (subconjunto usado en produccion). */
public enum PinBlockFormat {
    ISO_0("01"),
    ISO_1("02"),
    ISO_2("03"),
    ISO_3("04");

    private final String code;

    PinBlockFormat(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
