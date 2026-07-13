package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.HsmHealth;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;

import java.util.Map;

/** Comando NC - Diagnostics. Sin campos de request; se usa tambien como keep-alive/ping. */
public record DiagnosticsCommand() implements ThalesCommand<HsmHealth> {

    @Override
    public String requestCode() {
        return "NC";
    }

    @Override
    public String encodeBody() {
        return "";
    }

    @Override
    public HsmHealth parseResponse(String responseCode, String errorCode, String remainingFields) {
        boolean up = ThalesResponses.isSuccess(errorCode);
        if (!up) {
            return new HsmHealth(false, null, null, Map.of("errorCode", errorCode));
        }
        String lmkCheckValue = remainingFields.substring(0, 16);
        String firmwareNumber = remainingFields.substring(16, 25);
        return new HsmHealth(true, lmkCheckValue, firmwareNumber, Map.of());
    }
}
