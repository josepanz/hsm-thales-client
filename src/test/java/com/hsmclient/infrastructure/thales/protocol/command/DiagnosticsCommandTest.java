package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.HsmHealth;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticsCommandTest {

    @Test
    void requestHasNoBody() {
        var command = new DiagnosticsCommand();
        assertThat(command.requestCode()).isEqualTo("NC");
        assertThat(command.encodeBody()).isEmpty();
    }

    @Test
    void successfulResponseIsReportedAsUpWithFirmwareDetails() {
        var command = new DiagnosticsCommand();
        String lmkCheckValue = "AAAABBBBCCCCDDDD";
        String firmware = "0007-E000";

        HsmHealth health = command.parseResponse("ND", "00", lmkCheckValue + firmware);

        assertThat(health.up()).isTrue();
        assertThat(health.lmkCheckValue()).isEqualTo(lmkCheckValue);
        assertThat(health.firmwareNumber()).isEqualTo(firmware);
    }

    @Test
    void errorResponseIsReportedAsDownInsteadOfThrowing() {
        var command = new DiagnosticsCommand();

        HsmHealth health = command.parseResponse("ND", "17", "");

        assertThat(health.up()).isFalse();
        assertThat(health.details()).containsEntry("errorCode", "17");
    }
}
