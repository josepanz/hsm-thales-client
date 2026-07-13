package com.hsmclient.infrastructure.luna;

import com.hsmclient.domain.exception.HsmTransportException;
import com.hsmclient.domain.port.out.GenericCryptoPort;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;

/**
 * Adapter de ejemplo para Thales Luna Network HSM usando JCE puro (provider PKCS#11).
 * <p>
 * <b>Alcance:</b> este adapter NO reemplaza a {@code ThalesPaymentHsmAdapter}. Luna no
 * entiende los comandos de pago (CA/EA/BK/G0) ni el concepto de TPK/ZPK/PVK bajo un LMK
 * Variant - eso es especifico de la linea payShield. Lo que Luna si aporta es custodia de
 * llaves de proposito general y operaciones criptograficas estandar (firma, wrap de
 * llaves de aplicacion, generacion de pares de llaves) a traves de las APIs normales de
 * {@code java.security}, por eso implementa {@link GenericCryptoPort} y no
 * {@code PaymentHsmPort}. Usalo, por ejemplo, para firmar tokens o proteger la llave TLS
 * de un servicio - no para traducir un PIN block.
 */
public class LunaCryptoAdapter implements GenericCryptoPort {

    private final KeyStore keyStore;

    /**
     * TODO antes de instanciar esto contra un Luna real (ver docs/LUNA_SETUP_CHECKLIST.md):
     *   1. Cliente Luna (Chrystoki/cryptoki) instalado en este host/contenedor.
     *   2. {@code properties.getPkcs11ConfigPath()} apuntando a un archivo de config real,
     *      con la ruta a la libreria nativa y el slot/particion correctos.
     *   3. {@code properties.getKeyStorePassword()} con el PIN de la particion (via variable
     *      de entorno, no hardcodeado).
     * Mientras no tengas eso, usa {@link MockGenericCryptoAdapter} (hsm.luna.mock-mode=true).
     */
    public LunaCryptoAdapter(LunaProperties properties) {
        try {
            Provider provider = Security.getProvider("SunPKCS11");
            if (provider == null) {
                throw new IllegalStateException("Provider SunPKCS11 no disponible en esta JVM");
            }
            provider = provider.configure(properties.getPkcs11ConfigPath());
            Security.addProvider(provider);
            this.keyStore = KeyStore.getInstance("PKCS11", provider);
            this.keyStore.load(null, properties.getKeyStorePassword().toCharArray());
        } catch (GeneralSecurityException | IOException e) {
            throw new HsmTransportException("No se pudo inicializar el provider PKCS#11 de Luna", e);
        }
    }

    @Override
    public byte[] sign(String keyAlias, byte[] data, String signatureAlgorithm) {
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, null);
            Signature signature = Signature.getInstance(signatureAlgorithm, keyStore.getProvider());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("Fallo firmando con la llave '" + keyAlias + "' en Luna", e);
        }
    }

    @Override
    public byte[] wrapKey(String wrappingKeyAlias, byte[] keyMaterial, String wrapAlgorithm) {
        try {
            Key wrappingKey = keyStore.getKey(wrappingKeyAlias, null);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(wrapAlgorithm, keyStore.getProvider());
            cipher.init(javax.crypto.Cipher.WRAP_MODE, wrappingKey);
            SecretKey toWrap = new javax.crypto.spec.SecretKeySpec(keyMaterial, "AES");
            return cipher.wrap(toWrap);
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("Fallo envolviendo llave con '" + wrappingKeyAlias + "' en Luna", e);
        }
    }

    @Override
    public KeyPair generateKeyPair(String keyAlias, String algorithm, int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm, keyStore.getProvider());
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("Fallo generando par de llaves '" + keyAlias + "' en Luna", e);
        }
    }
}
