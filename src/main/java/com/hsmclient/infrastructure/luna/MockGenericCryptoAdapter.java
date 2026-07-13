package com.hsmclient.infrastructure.luna;

import com.hsmclient.domain.exception.HsmTransportException;
import com.hsmclient.domain.port.out.GenericCryptoPort;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacion "software" de {@link GenericCryptoPort}: usa los providers JCE por defecto
 * de la JVM (SunRsaSign/SunJCE) en vez de un provider PKCS#11 real. Sirve para desarrollar y
 * correr tests de punta a punta sin tener un Luna Network HSM (ni su simulador) conectado.
 * <p>
 * <b>NUNCA usar en produccion.</b> Las llaves privadas viven en memoria de la JVM, sin
 * proteccion de hardware - exactamente lo que un HSM existe para evitar. Se activa solo con
 * {@code hsm.luna.enabled=true} + {@code hsm.luna.mock-mode=true} (ver {@link LunaProperties}),
 * y el default es {@code false} para que nadie lo prenda por accidente.
 * <p>
 * Limitacion a proposito (para no complicar el mock de mas): solo soporta flujos con llaves
 * asimetricas (un {@link KeyPair} por alias, generado con {@link #generateKeyPair}). No modela
 * llaves simetricas persistidas por alias como si podria hacer un Luna real. Si tu caso de uso
 * necesita eso, es una extension chica: agregar un {@code Map<String, SecretKey>} igual al de
 * los KeyPair y usarlo en {@link #wrapKey} cuando el alias no sea un KeyPair.
 */
public class MockGenericCryptoAdapter implements GenericCryptoPort {

    private final Map<String, KeyPair> keyPairsByAlias = new ConcurrentHashMap<>();

    @Override
    public byte[] sign(String keyAlias, byte[] data, String signatureAlgorithm) {
        KeyPair pair = requireKeyPair(keyAlias);
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(pair.getPrivate());
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("[mock] Fallo firmando con la llave '" + keyAlias + "'", e);
        }
    }

    @Override
    public byte[] wrapKey(String wrappingKeyAlias, byte[] keyMaterial, String wrapAlgorithm) {
        KeyPair pair = requireKeyPair(wrappingKeyAlias);
        try {
            Cipher cipher = Cipher.getInstance(wrapAlgorithm);
            cipher.init(Cipher.WRAP_MODE, pair.getPublic());
            SecretKeySpec toWrap = new SecretKeySpec(keyMaterial, "AES");
            return cipher.wrap(toWrap);
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("[mock] Fallo envolviendo llave con '" + wrappingKeyAlias + "'", e);
        }
    }

    @Override
    public KeyPair generateKeyPair(String keyAlias, String algorithm, int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
            generator.initialize(keySize);
            KeyPair pair = generator.generateKeyPair();
            keyPairsByAlias.put(keyAlias, pair);
            return pair;
        } catch (GeneralSecurityException e) {
            throw new HsmTransportException("[mock] Fallo generando par de llaves '" + keyAlias + "'", e);
        }
    }

    private KeyPair requireKeyPair(String alias) {
        KeyPair pair = keyPairsByAlias.get(alias);
        if (pair == null) {
            throw new HsmTransportException(
                    "[mock] No existe ningun par de llaves con alias '" + alias +
                            "' - llama generateKeyPair(alias, ...) antes de usarlo");
        }
        return pair;
    }
}
