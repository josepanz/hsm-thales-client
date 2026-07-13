package com.hsmclient.config;

import com.hsmclient.domain.port.out.GenericCryptoPort;
import com.hsmclient.infrastructure.luna.LunaCryptoAdapter;
import com.hsmclient.infrastructure.luna.LunaProperties;
import com.hsmclient.infrastructure.luna.MockGenericCryptoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Deshabilitado por defecto: la mayoria de los despliegues de este cliente solo necesitan
 * el HSM de pagos (Thales payShield, ver {@link ThalesConfig}). Habilitar solo si de verdad
 * hay un Luna Network HSM disponible (o su mock, para desarrollo) para operaciones
 * criptograficas genericas.
 * <p>
 * TODO paso a paso para pasar de "estoy probando esto en mi maquina" a "esto habla con un
 * Luna real" - ver tambien docs/LUNA_SETUP_CHECKLIST.md:
 *   1. HOY (sin hardware): hsm.luna.enabled=true, hsm.luna.mock-mode=true -> usa
 *      {@link MockGenericCryptoAdapter} (JCE software, ver esa clase).
 *   2. CUANDO TENGAS el cliente Luna instalado y un cfg PKCS#11 real: cambia SOLO
 *      hsm.luna.mock-mode=false (deja enabled=true) -> pasa automaticamente a
 *      {@link LunaCryptoAdapter} sin tocar codigo, ver el metodo genericCryptoPort() de abajo.
 *   3. Completa hsm.luna.pkcs11-config-path y hsm.luna.key-store-password apuntando al Luna real.
 */
@Configuration
@EnableConfigurationProperties(LunaProperties.class)
@ConditionalOnProperty(prefix = "hsm.luna", name = "enabled", havingValue = "true")
public class LunaConfig {

    private static final Logger log = LoggerFactory.getLogger(LunaConfig.class);

    @Bean
    public GenericCryptoPort genericCryptoPort(LunaProperties properties) {
        if (properties.isMockMode()) {
            // TODO: cuando tengas un Luna real, poner hsm.luna.mock-mode=false - no requiere
            // cambiar nada de codigo, solo esa propiedad. Ver el bloque de arriba.
            log.warn("hsm.luna.mock-mode=true: usando MockGenericCryptoAdapter (JCE software, SIN hardware real). "
                    + "No usar esta configuracion en produccion.");
            return new MockGenericCryptoAdapter();
        }
        return new LunaCryptoAdapter(properties);
    }
}
