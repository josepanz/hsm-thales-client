package com.hsmclient.infrastructure.thales.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ThalesFrameTest {

    @Test
    void writeThenReadRoundTripsThePayload() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ThalesFrame.write(new DataOutputStream(buffer), "000000000001NC");

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        assertThat(ThalesFrame.read(in)).isEqualTo("000000000001NC");
    }

    @Test
    void lengthPrefixIsTwoBytesBigEndian() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ThalesFrame.write(new DataOutputStream(buffer), "AB"); // payload de 2 bytes

        byte[] framed = buffer.toByteArray();
        assertThat(framed[0]).isEqualTo((byte) 0);
        assertThat(framed[1]).isEqualTo((byte) 2);
        assertThat(framed).hasSize(4); // 2 bytes de longitud + 2 bytes de payload
    }
}
