package com.hsmclient.infrastructure.thales;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Propiedades de conexion al HSM Thales (payShield 9000/10000, modo Variant LMK).
 * Ver application.yml, prefijo "hsm.thales".
 */
@ConfigurationProperties(prefix = "hsm.thales")
public class ThalesProperties {

    /** Lista de "host:port" de los nodos HSM. Mas de uno habilita failover round-robin. */
    private List<String> nodes = List.of();

    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration requestTimeout = Duration.ofSeconds(5);
    private int socketTimeoutMillis = 900_000;
    private Duration healthCheckInterval = Duration.ofSeconds(30);

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }
}
