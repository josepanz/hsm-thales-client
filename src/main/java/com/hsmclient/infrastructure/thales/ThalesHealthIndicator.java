package com.hsmclient.infrastructure.thales;

import com.hsmclient.domain.model.HsmHealth;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/** Expone el resultado del comando NC (Diagnostics) como /actuator/health/thales. */
public class ThalesHealthIndicator implements HealthIndicator {

    private final PaymentHsmPort hsm;

    public ThalesHealthIndicator(PaymentHsmPort hsm) {
        this.hsm = hsm;
    }

    @Override
    public Health health() {
        try {
            HsmHealth h = hsm.diagnostics();
            Health.Builder builder = h.up() ? Health.up() : Health.down();
            if (h.firmwareNumber() != null) {
                builder.withDetail("firmwareNumber", h.firmwareNumber());
            }
            if (h.lmkCheckValue() != null) {
                builder.withDetail("lmkCheckValue", h.lmkCheckValue());
            }
            return builder.build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
