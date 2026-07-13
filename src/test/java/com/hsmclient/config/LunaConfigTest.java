package com.hsmclient.config;

import com.hsmclient.domain.port.out.GenericCryptoPort;
import com.hsmclient.infrastructure.luna.MockGenericCryptoAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lo unico verificable sin un HSM Luna real: que el wiring condicional se comporte bien.
 * El adapter en si necesita hardware/software PKCS#11 real, ver LunaPropertiesTest.
 */
class LunaConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LunaConfig.class);

    @Test
    void noGenericCryptoPortBeanWhenDisabled() {
        contextRunner
                .withPropertyValues("hsm.luna.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GenericCryptoPort.class));
    }

    @Test
    void noGenericCryptoPortBeanWhenPropertyIsAbsent() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(GenericCryptoPort.class));
    }

    @Test
    void enablingWithoutARealPkcs11ProviderFailsFastInsteadOfStartingSilently() {
        contextRunner
                .withPropertyValues(
                        "hsm.luna.enabled=true",
                        "hsm.luna.mock-mode=false",
                        "hsm.luna.pkcs11-config-path=/nonexistent/pkcs11-luna.cfg",
                        "hsm.luna.key-store-password=unused")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void mockModeWiresTheSoftwareAdapterWithoutNeedingRealHardware() {
        contextRunner
                .withPropertyValues("hsm.luna.enabled=true", "hsm.luna.mock-mode=true")
                .run(context -> assertThat(context.getBean(GenericCryptoPort.class))
                        .isInstanceOf(MockGenericCryptoAdapter.class));
    }
}
