package com.hsmclient.infrastructure.luna;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades del provider PKCS#11 de Thales Luna Network HSM.
 * <p>
 * Luna se integra vulnerabilidad-cero via JCE estandar: se registra un
 * {@code java.security.Provider} (tipicamente {@code sun.security.pkcs11.SunPKCS11}) apuntando
 * a la libreria nativa del cliente Luna (Chrystoki/cryptoki .so/.dll) y a un slot/particion,
 * y desde ahi se usan {@code KeyStore}, {@code Cipher}, {@code Signature} y
 * {@code KeyPairGenerator} normales de JCE. No hay protocolo de mensajes propietario como
 * en payShield: todo pasa por las APIs estandar de java.security.
 */
@ConfigurationProperties(prefix = "hsm.luna")
public class LunaProperties {

    private boolean enabled = false;

    /**
     * Si es true, se usa {@link MockGenericCryptoAdapter} (JCE software, sin PKCS#11 real) en
     * vez de {@link LunaCryptoAdapter}. Pensado para desarrollo local/CI donde no hay un HSM
     * Luna (ni su simulador) disponible. Default false a proposito: habilitar "enabled=true"
     * sin querer decir explicitamente "mock-mode=true" NO debe terminar corriendo software
     * fingiendo ser un HSM sin que alguien lo haya pedido a proposito.
     * <p>
     * TODO (paso a paso para pasar de mock a Luna real, ver docs/LUNA_SETUP_CHECKLIST.md):
     *   1. Instalar el cliente Luna (Chrystoki/cryptoki) en el host/contenedor.
     *   2. Crear el archivo de config PKCS#11 real y apuntar {@link #pkcs11ConfigPath} a el.
     *   3. Setear {@link #keyStorePassword} (el PIN/password de la particion Luna), idealmente
     *      desde una variable de entorno, nunca hardcodeado.
     *   4. Poner mock-mode=false (o simplemente borrar la propiedad, false es el default).
     *   5. Correr un smoke test manual contra LunaCryptoAdapter antes de confiar en produccion.
     */
    private boolean mockMode = false;

    /** Ruta al archivo de configuracion del provider PKCS#11 (ver pkcs11-luna.cfg de ejemplo en docs/). */
    private String pkcs11ConfigPath;

    private String keyStorePassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public String getPkcs11ConfigPath() {
        return pkcs11ConfigPath;
    }

    public void setPkcs11ConfigPath(String pkcs11ConfigPath) {
        this.pkcs11ConfigPath = pkcs11ConfigPath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }
}
