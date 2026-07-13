package com.hsmclient.application.service;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.GeneratedKey;
import com.hsmclient.domain.model.KeyScheme;
import com.hsmclient.domain.model.RsaKeyPair;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyManagementServiceTest {

    @Mock
    private PaymentHsmPort hsm;

    @Test
    void generateZmkDelegatesToThePort() {
        GeneratedKey expected = new GeneratedKey(EncryptedKey.of("AAAABBBBCCCCDDDDEEEEFFFF00001111"), "12CD34");
        when(hsm.generateZmk(KeyScheme.DOUBLE_LENGTH)).thenReturn(expected);

        KeyManagementService service = new KeyManagementService(hsm);

        assertThat(service.generateZmk(KeyScheme.DOUBLE_LENGTH)).isEqualTo(expected);
    }

    @Test
    void generateRsaKeyPairDelegatesToThePort() {
        RsaKeyPair expected = new RsaKeyPair(new byte[]{1, 2}, new byte[]{3, 4});
        when(hsm.generateRsaKeyPair(2048)).thenReturn(expected);

        KeyManagementService service = new KeyManagementService(hsm);

        assertThat(service.generateRsaKeyPair(2048)).isEqualTo(expected);
    }
}
