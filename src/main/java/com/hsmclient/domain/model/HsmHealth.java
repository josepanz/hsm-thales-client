package com.hsmclient.domain.model;

import java.util.Map;

/** Resultado de un chequeo de salud del HSM (comando NC - Diagnostics). */
public record HsmHealth(boolean up, String lmkCheckValue, String firmwareNumber, Map<String, String> details) {
}
