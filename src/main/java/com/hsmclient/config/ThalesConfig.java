package com.hsmclient.config;

import com.hsmclient.domain.port.out.PaymentHsmPort;
import com.hsmclient.infrastructure.thales.ThalesHealthIndicator;
import com.hsmclient.infrastructure.thales.ThalesPaymentHsmAdapter;
import com.hsmclient.infrastructure.thales.ThalesProperties;
import com.hsmclient.infrastructure.thales.transport.ThalesConnectionPool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ThalesProperties.class)
public class ThalesConfig {

    @Bean(destroyMethod = "close")
    public ThalesConnectionPool thalesConnectionPool(ThalesProperties properties) {
        return new ThalesConnectionPool(
                properties.getNodes(),
                properties.getConnectTimeout(),
                properties.getSocketTimeoutMillis(),
                properties.getHealthCheckInterval());
    }

    @Bean
    public PaymentHsmPort paymentHsmPort(ThalesConnectionPool pool, ThalesProperties properties) {
        return new ThalesPaymentHsmAdapter(pool, properties.getRequestTimeout());
    }

    @Bean
    public ThalesHealthIndicator thalesHealthIndicator(PaymentHsmPort hsm) {
        return new ThalesHealthIndicator(hsm);
    }
}
