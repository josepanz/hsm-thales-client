package com.hsmclient.domain.model;

/** Resultado del comando EI (Generate an RSA Key Pair): clave publica en claro y privada cifrada bajo LMK. */
public record RsaKeyPair(byte[] publicKey, byte[] encryptedPrivateKey) {
}
