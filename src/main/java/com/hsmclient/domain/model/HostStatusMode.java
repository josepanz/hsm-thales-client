package com.hsmclient.domain.model;

/** Modo del comando NO (Host Status). 00 = info de red/firmware, 01 = compliance PCI-HSM. */
public enum HostStatusMode {
    NETWORK_INFO("00"),
    PCI_HSM_COMPLIANCE("01");

    private final String code;

    HostStatusMode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
