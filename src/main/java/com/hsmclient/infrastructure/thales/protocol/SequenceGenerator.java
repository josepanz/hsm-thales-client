package com.hsmclient.infrastructure.thales.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Genera el header numerico de 12 digitos que correla cada request con su response.
 * <p>
 * El cliente original delegaba esto a {@code SpaceUtil.nextLong} y usaba el header
 * como key de una cola de espacio (jPOS Space) para hacer el matching request/response;
 * funciona, pero acopla la correlacion al mecanismo de colas de jPOS. Aca el mismo
 * numero de 12 digitos se usa como key de un mapa de correlacion en memoria
 * ({@code ThalesConnection}), sin depender de ese framework.
 */
public final class SequenceGenerator {

    private static final long MODULO = 1_000_000_000_000L; // 10^12
    private final AtomicLong counter = new AtomicLong();

    public String next() {
        long value = Math.abs(counter.incrementAndGet()) % MODULO;
        return String.format("%012d", value);
    }
}
