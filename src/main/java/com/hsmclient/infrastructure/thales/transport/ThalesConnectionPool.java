package com.hsmclient.infrastructure.thales.transport;

import com.hsmclient.domain.exception.HsmTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pool round-robin de conexiones a uno o mas host:port del HSM.
 * <p>
 * El reparto de requests entre nodos es explicito por round-robin, y hay reconexion
 * automatica en background si un nodo cae.
 */
public class ThalesConnectionPool implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ThalesConnectionPool.class);

    private final List<ThalesConnection> connections = new CopyOnWriteArrayList<>();
    private final AtomicInteger cursor = new AtomicInteger();
    private final ScheduledExecutorService healthChecker = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r, "thales-pool-health-check");
                t.setDaemon(true);
                return t;
            });

    private final Duration connectTimeout;
    private final int soTimeoutMillis;
    private final Duration healthCheckInterval;

    public ThalesConnectionPool(List<String> hostPorts, Duration connectTimeout, int soTimeoutMillis,
                                 Duration healthCheckInterval) {
        if (hostPorts == null || hostPorts.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un host:port de HSM configurado");
        }
        this.connectTimeout = connectTimeout;
        this.soTimeoutMillis = soTimeoutMillis;
        this.healthCheckInterval = healthCheckInterval;
        hostPorts.forEach(this::addConnection);
        healthChecker.scheduleWithFixedDelay(this::reconnectUnhealthy,
                healthCheckInterval.toSeconds(), healthCheckInterval.toSeconds(), TimeUnit.SECONDS);
    }

    private void addConnection(String hostPort) {
        String[] parts = hostPort.split(":");
        ThalesConnection connection = new ThalesConnection(parts[0], Integer.parseInt(parts[1]));
        try {
            connection.connect(connectTimeout, soTimeoutMillis);
        } catch (HsmTransportException e) {
            log.warn("No se pudo conectar de entrada a {}, se reintentara en background: {}", hostPort, e.getMessage());
        }
        connections.add(connection);
    }

    private void reconnectUnhealthy() {
        for (ThalesConnection c : connections) {
            if (!c.isHealthy()) {
                try {
                    log.info("Reconectando a {}:{}", c.host(), c.port());
                    c.connect(connectTimeout, soTimeoutMillis);
                } catch (Exception e) {
                    log.warn("Fallo la reconexion a {}:{}: {}", c.host(), c.port(), e.getMessage());
                }
            }
        }
    }

    /** Devuelve la siguiente conexion saludable en round-robin; falla si ninguna lo esta. */
    public ThalesConnection acquire() {
        int size = connections.size();
        for (int i = 0; i < size; i++) {
            int index = Math.floorMod(cursor.getAndIncrement(), size);
            ThalesConnection candidate = connections.get(index);
            if (candidate.isHealthy()) {
                return candidate;
            }
        }
        throw new HsmTransportException("Ningun nodo HSM disponible en el pool (" + size + " configurados)");
    }

    @Override
    public void close() {
        healthChecker.shutdownNow();
        connections.forEach(ThalesConnection::close);
    }
}
