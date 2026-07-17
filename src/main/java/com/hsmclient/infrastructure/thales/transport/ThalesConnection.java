package com.hsmclient.infrastructure.thales.transport;

import com.hsmclient.domain.exception.HsmTimeoutException;
import com.hsmclient.domain.exception.HsmTransportException;
import com.hsmclient.infrastructure.thales.protocol.SequenceGenerator;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Una conexion TCP a un host:port del HSM, con un hilo lector dedicado que demultiplexa
 * las respuestas por el header de 12 digitos.
 * <p>
 * Usa un mapa de correlacion en memoria + {@link CompletableFuture} en vez de polling sobre
 * una cola compartida: menor latencia (el lector reacciona apenas llega el frame) y sin
 * acoplar el transporte a un framework de colas.
 * <p>
 * Thread-safety: {@link #send} puede llamarse concurrentemente desde varios hilos de la
 * aplicacion; la escritura al socket esta serializada con un lock dedicado para que dos
 * requests concurrentes nunca intercalen sus bytes en el mismo frame.
 */
public class ThalesConnection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ThalesConnection.class);

    private final String host;
    private final int port;
    private final SequenceGenerator sequence = new SequenceGenerator();
    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    private volatile Socket socket;
    private volatile DataInputStream in;
    private volatile DataOutputStream out;
    private volatile Thread readerThread;
    private volatile boolean closed;

    public ThalesConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect(Duration connectTimeout, int soTimeoutMillis) {
        try {
            Socket s = new Socket();
            s.connect(new java.net.InetSocketAddress(host, port), (int) connectTimeout.toMillis());
            s.setSoTimeout(soTimeoutMillis);
            this.socket = s;
            this.in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            this.out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream(), 4096));
            this.closed = false;
            this.readerThread = new Thread(this::readLoop, "thales-reader-" + host + ":" + port);
            this.readerThread.setDaemon(true);
            this.readerThread.start();
            log.info("Conectado a HSM Thales {}:{}", host, port);
        } catch (IOException e) {
            throw new HsmTransportException("No se pudo conectar a " + host + ":" + port, e);
        }
    }

    public boolean isHealthy() {
        return !closed && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public <R> R send(ThalesCommand<R> command, Duration timeout) {
        if (!isHealthy()) {
            throw new HsmTransportException("Conexion a " + host + ":" + port + " no disponible");
        }
        String header = sequence.next();
        String requestBody = header + command.requestCode() + command.encodeBody();
        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(header, future);
        try {
            synchronized (writeLock) {
                ThalesFrame.write(out, requestBody);
            }
            String responseBody = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            String responseCode = responseBody.substring(0, 2);
            String errorCode = responseBody.substring(2, 4);
            String remaining = responseBody.substring(4);
            return command.parseResponse(responseCode, errorCode, remaining);
        } catch (TimeoutException e) {
            throw new HsmTimeoutException(command.requestCode(), timeout.toMillis());
        } catch (IOException e) {
            close();
            throw new HsmTransportException("Error de I/O enviando comando '" + command.requestCode() + "'", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HsmTransportException("Interrumpido esperando respuesta de '" + command.requestCode() + "'", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new HsmTransportException("Error inesperado esperando respuesta de '" + command.requestCode() + "'", e.getCause());
        } finally {
            pending.remove(header);
        }
    }

    private void readLoop() {
        try {
            while (!closed) {
                String frame = ThalesFrame.read(in);
                if (frame.length() < 12) {
                    log.warn("Frame demasiado corto para contener header: '{}'", frame);
                    continue;
                }
                String header = frame.substring(0, 12);
                String body = frame.substring(12);
                CompletableFuture<String> future = pending.remove(header);
                if (future != null) {
                    future.complete(body);
                } else {
                    log.warn("Respuesta sin request pendiente para header '{}'", header);
                }
            }
        } catch (IOException e) {
            if (!closed) {
                log.warn("Conexion a {}:{} perdida: {}", host, port, e.getMessage());
                close();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        pending.values().forEach(f -> f.completeExceptionally(new HsmTransportException("Conexion cerrada")));
        pending.clear();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            log.debug("Error cerrando socket", e);
        }
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
