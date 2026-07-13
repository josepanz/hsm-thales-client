package com.hsmclient.infrastructure.luna;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solo prueba el binding de propiedades. El adapter en si (LunaCryptoAdapter) necesita un
 * provider PKCS#11 real (libreria nativa del cliente Luna + HSM o simulador conectado) para
 * hacer algo verificable - no hay forma honesta de unit-testearlo sin ese hardware, ver
 * LunaConfigTest para lo que si se puede probar sin el (el wiring condicional del bean).
 */
class LunaPropertiesTest {

    @Test
    void disabledByDefault() {
        assertThat(new LunaProperties().isEnabled()).isFalse();
    }

    @Test
    void exposesConfiguredValues() {
        LunaProperties properties = new LunaProperties();
        properties.setEnabled(true);
        properties.setPkcs11ConfigPath("/etc/hsm/pkcs11-luna.cfg");
        properties.setKeyStorePassword("secret");

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getPkcs11ConfigPath()).isEqualTo("/etc/hsm/pkcs11-luna.cfg");
        assertThat(properties.getKeyStorePassword()).isEqualTo("secret");
    }
}
