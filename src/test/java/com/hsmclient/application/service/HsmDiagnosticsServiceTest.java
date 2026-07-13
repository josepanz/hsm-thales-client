package com.hsmclient.application.service;

import com.hsmclient.domain.model.HostStatus;
import com.hsmclient.domain.model.HostStatusMode;
import com.hsmclient.domain.model.HsmHealth;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HsmDiagnosticsServiceTest {

    @Mock
    private PaymentHsmPort hsm;

    @Test
    void checkHealthDelegatesToThePort() {
        HsmHealth expected = new HsmHealth(true, "AAAABBBBCCCCDDDD", "0007-E000", Map.of());
        when(hsm.diagnostics()).thenReturn(expected);

        HsmDiagnosticsService service = new HsmDiagnosticsService(hsm);

        assertThat(service.checkHealth()).isEqualTo(expected);
    }

    @Test
    void hostStatusDelegatesToThePort() {
        HostStatus expected = new HostStatus(HostStatusMode.PCI_HSM_COMPLIANCE, true, Map.of());
        when(hsm.hostStatus(HostStatusMode.PCI_HSM_COMPLIANCE)).thenReturn(expected);

        HsmDiagnosticsService service = new HsmDiagnosticsService(hsm);

        assertThat(service.hostStatus(HostStatusMode.PCI_HSM_COMPLIANCE)).isEqualTo(expected);
    }
}
