package com.hsmclient.application.service;

import com.hsmclient.domain.model.HostStatus;
import com.hsmclient.domain.model.HostStatusMode;
import com.hsmclient.domain.model.HsmHealth;
import com.hsmclient.domain.port.in.HsmDiagnosticsUseCase;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.springframework.stereotype.Service;

@Service
public class HsmDiagnosticsService implements HsmDiagnosticsUseCase {

    private final PaymentHsmPort hsm;

    public HsmDiagnosticsService(PaymentHsmPort hsm) {
        this.hsm = hsm;
    }

    @Override
    public HsmHealth checkHealth() {
        return hsm.diagnostics();
    }

    @Override
    public HostStatus hostStatus(HostStatusMode mode) {
        return hsm.hostStatus(mode);
    }
}
