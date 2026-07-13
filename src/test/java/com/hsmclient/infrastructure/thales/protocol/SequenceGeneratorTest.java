package com.hsmclient.infrastructure.thales.protocol;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El header de 12 digitos es lo unico que correla un request con su response; si dos
 * requests concurrentes generan el mismo header, una respuesta se le entrega al caller
 * equivocado. Por eso esto se prueba bajo concurrencia real, no solo en un loop secuencial.
 */
class SequenceGeneratorTest {

    @Test
    void alwaysProduces12Digits() {
        SequenceGenerator generator = new SequenceGenerator();
        assertThat(generator.next()).hasSize(12);
        assertThat(generator.next()).matches("^[0-9]{12}$");
    }

    @Test
    void isUniqueUnderConcurrentAccess() throws InterruptedException {
        SequenceGenerator generator = new SequenceGenerator();
        int callers = 16;
        int perCaller = 500;
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        Set<String> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        AtomicInteger duplicates = new AtomicInteger();

        IntStream.range(0, callers).forEach(i -> pool.submit(() -> {
            for (int j = 0; j < perCaller; j++) {
                if (!seen.add(generator.next())) {
                    duplicates.incrementAndGet();
                }
            }
        }));

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(duplicates.get()).isZero();
        assertThat(seen).hasSize(callers * perCaller);
    }
}
