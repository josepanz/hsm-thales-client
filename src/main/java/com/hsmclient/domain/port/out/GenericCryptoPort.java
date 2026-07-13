package com.hsmclient.domain.port.out;

import java.security.KeyPair;

/**
 * Puerto de salida para operaciones criptograficas GENERICAS (custodia de llaves, firma,
 * cifrado de datos de aplicacion) respaldadas por un HSM de proposito general como Thales Luna.
 * <p>
 * <b>Importante - fuera de alcance:</b> Luna Network HSM habla PKCS#11/JCE/KMIP, NO el protocolo
 * de comandos de pago de Thales payShield (CA/EA/BK/G0/...). No existe traduccion de PIN block,
 * validacion de PVK/offset ni derivacion DUKPT en Luna: esas son funciones especificas de la
 * linea payShield. Este puerto modela unicamente lo que Luna si puede hacer (firma, wrap/unwrap
 * de llaves de aplicacion, generacion de pares de llaves) para casos que no son PIN translate,
 * por ejemplo firmar tokens, proteger llaves de un microservicio, o TLS mutual auth con llave
 * custodiada en hardware. Ver domain/port/out/PaymentHsmPort para el dominio de pagos real.
 */
public interface GenericCryptoPort {

    byte[] sign(String keyAlias, byte[] data, String signatureAlgorithm);

    byte[] wrapKey(String wrappingKeyAlias, byte[] keyMaterial, String wrapAlgorithm);

    KeyPair generateKeyPair(String keyAlias, String algorithm, int keySize);
}
