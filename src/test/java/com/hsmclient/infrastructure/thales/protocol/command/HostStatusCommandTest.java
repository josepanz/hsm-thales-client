package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.HostStatus;
import com.hsmclient.domain.model.HostStatusMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HostStatusCommandTest {

    @Test
    void mode00EncodesNetworkInfoModeFlag() {
        var command = new HostStatusCommand(HostStatusMode.NETWORK_INFO);
        assertThat(command.requestCode()).isEqualTo("NO");
        assertThat(command.encodeBody()).isEqualTo("00");
    }

    @Test
    void mode00ParsesNetworkDetails() {
        var command = new HostStatusCommand(HostStatusMode.NETWORK_INFO);
        // io-buffer-size(1) + ethernet-type(1) + number-of-tcp-sockets(2) + firmware-number(9)
        String body = "1" + "0" + "04" + "0007-E000";

        HostStatus status = command.parseResponse("NP", "00", body);

        assertThat(status.details())
                .containsEntry("number-of-tcp-sockets", "04")
                .containsEntry("firmware-number", "0007-E000");
    }

    @Test
    void mode01ParsesPciHsmCompliance() {
        var command = new HostStatusCommand(HostStatusMode.PCI_HSM_COMPLIANCE);

        HostStatus status = command.parseResponse("NP", "00", "1" + "0".repeat(10));

        assertThat(status.pciHsmCompliant()).isTrue();
    }
}
