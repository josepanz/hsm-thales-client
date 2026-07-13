package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.HostStatus;
import com.hsmclient.domain.model.HostStatusMode;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;

import java.util.LinkedHashMap;
import java.util.Map;

/** Comando NO - Host Status. Modo 00 = info de red/firmware, modo 01 = compliance PCI-HSM. */
public record HostStatusCommand(HostStatusMode mode) implements ThalesCommand<HostStatus> {

    @Override
    public String requestCode() {
        return "NO";
    }

    @Override
    public String encodeBody() {
        return mode.code();
    }

    @Override
    public HostStatus parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        if (mode == HostStatusMode.PCI_HSM_COMPLIANCE) {
            boolean compliant = "1".equals(remainingFields.substring(0, 1));
            return new HostStatus(mode, compliant, Map.of());
        }
        Map<String, String> details = new LinkedHashMap<>();
        details.put("io-buffer-size", remainingFields.substring(0, 1));
        details.put("ethernet-type", remainingFields.substring(1, 2));
        details.put("number-of-tcp-sockets", remainingFields.substring(2, 4));
        details.put("firmware-number", remainingFields.substring(4, 13));
        return new HostStatus(mode, false, details);
    }
}
