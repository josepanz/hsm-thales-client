package com.hsmclient.infrastructure.thales.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Genera el header numerico de 12 digitos que correla cada request con su response.
 * <p>
 * Ese numero se usa como key de un mapa de correlacion en memoria
 * ({@code ThalesConnection}), sin depender de un framework de colas externo.
 */
public final class SequenceGenerator {

    private static final long MODULO = 1_000_000_000_000L; // 10^12
    private final AtomicLong counter = new AtomicLong();

    public String next() {
        long value = Math.abs(counter.incrementAndGet()) % MODULO;
        return String.format("%012d", value);
    }
}
