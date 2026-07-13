package com.hsmclient.infrastructure.thales.protocol.codec;

import java.util.HexFormat;

public final class Hex {

    private Hex() {
    }

    public static byte[] decode(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    public static String encode(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
