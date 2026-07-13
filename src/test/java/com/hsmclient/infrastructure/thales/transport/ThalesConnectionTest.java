package com.hsmclient.infrastructure.thales.transport;

import com.hsmclient.domain.exception.HsmTimeoutException;
import com.hsmclient.domain.model.HsmHealth;
import com.hsmclient.infrastructure.thales.protocol.ThalesFrame;
import com.hsmclient.infrastructure.thales.protocol.command.DiagnosticsCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * En vez de mockear el socket, se levanta un servidor TCP loopback real que habla el
 * framing Thales (2 bytes de longitud + payload). Esto prueba de punta a punta el
 * mismo camino que se usaria contra un payShield real: framing, correlacion por header
 * y timeout, sin necesitar hardware.
 */
class ThalesConnectionTest {

    private ServerSocket server;
    private ThalesConnection connection;

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) connection.close();
        if (server != null) server.close();
    }

    @Test
    void sendCorrelatesResponseByHeaderAndParsesIt() throws Exception {
        server = new ServerSocket(0);

        Executors.newSingleThreadExecutor().submit(() -> {
            try (Socket client = server.accept()) {
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                String request = ThalesFrame.read(in); // header(12) + "NC"
                String header = request.substring(0, 12);
                String lmkCheckValue = "AAAABBBBCCCCDDDD"; // 16 hex
                String firmware = "0007-E000"; // 9 chars
                String response = header + "ND" + "00" + lmkCheckValue + firmware;
                out.writeShort(response.length());
                out.write(response.getBytes());
                out.flush();
            } catch (Exception ignored) {
            }
        });

        connection = new ThalesConnection("localhost", server.getLocalPort());
        connection.connect(Duration.ofSeconds(2), 5000);

        HsmHealth health = connection.send(new DiagnosticsCommand(), Duration.ofSeconds(2));

        assertThat(health.up()).isTrue();
        assertThat(health.lmkCheckValue()).isEqualTo("AAAABBBBCCCCDDDD");
        assertThat(health.firmwareNumber()).isEqualTo("0007-E000");
    }

    @Test
    void sendTimesOutWhenNoResponseArrives() throws Exception {
        server = new ServerSocket(0);
        Executors.newSingleThreadExecutor().submit(() -> {
            try (Socket client = server.accept()) {
                Thread.sleep(5000); // nunca responde dentro del timeout del test
            } catch (Exception ignored) {
            }
        });

        connection = new ThalesConnection("localhost", server.getLocalPort());
        connection.connect(Duration.ofSeconds(2), 5000);

        assertThatThrownBy(() -> connection.send(new DiagnosticsCommand(), Duration.ofMillis(300)))
                .isInstanceOf(HsmTimeoutException.class);
    }
}
