package com.hsmclient.domain.port.in;

import com.hsmclient.domain.model.HostStatus;
import com.hsmclient.domain.model.HostStatusMode;
import com.hsmclient.domain.model.HsmHealth;

public interface HsmDiagnosticsUseCase {

    /** Comando Thales NC. */
    HsmHealth checkHealth();

    /** Comando Thales NO. */
    HostStatus hostStatus(HostStatusMode mode);
}
