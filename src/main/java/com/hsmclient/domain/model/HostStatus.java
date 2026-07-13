package com.hsmclient.domain.model;

import java.util.Map;

/** Resultado del comando NO (Host Status), forma generica valida para ambos modos (00/01). */
public record HostStatus(HostStatusMode mode, boolean pciHsmCompliant, Map<String, String> details) {
}
