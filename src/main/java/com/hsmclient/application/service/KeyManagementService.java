package com.hsmclient.application.service;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.GeneratedKey;
import com.hsmclient.domain.model.KeyScheme;
import com.hsmclient.domain.model.RsaKeyPair;
import com.hsmclient.domain.port.in.KeyManagementUseCase;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.springframework.stereotype.Service;

@Service
public class KeyManagementService implements KeyManagementUseCase {

    private final PaymentHsmPort hsm;

    public KeyManagementService(PaymentHsmPort hsm) {
        this.hsm = hsm;
    }

    @Override
    public GeneratedKey generateZmk(KeyScheme scheme) {
        return hsm.generateZmk(scheme);
    }

    @Override
    public GeneratedKey generateZmkUnderZmk(EncryptedKey wrappingZmk) {
        return hsm.generateZmkUnderZmk(wrappingZmk);
    }

    @Override
    public RsaKeyPair generateRsaKeyPair(int keyLengthBits) {
        return hsm.generateRsaKeyPair(keyLengthBits);
    }
}
