package com.hsmclient.infrastructure.thales.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Framing de mensajes Thales: 2 bytes de longitud (big-endian) seguidos del payload
 * ASCII. Igual que el adapter original, solo que separado en su propia clase testeable
 * sin necesitar un socket real.
 */
public final class ThalesFrame {

    private ThalesFrame() {
    }

    public static void write(DataOutputStream out, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.US_ASCII);
        out.write(bytes.length >> 8);
        out.write(bytes.length);
        out.write(bytes);
        out.flush();
    }

    public static String read(DataInputStream in) throws IOException {
        int length = ((in.readUnsignedByte()) << 8) | in.readUnsignedByte();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }
}
