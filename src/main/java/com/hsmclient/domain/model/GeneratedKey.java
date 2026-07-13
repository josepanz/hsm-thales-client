package com.hsmclient.domain.model;

/** Resultado del comando A0 (Generate a Key): llave cifrada bajo LMK (o bajo un ZMK) + su check value. */
public record GeneratedKey(EncryptedKey key, String checkValue) {
}
