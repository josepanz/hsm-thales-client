package com.hsmclient.application.service;

import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.in.TranslatePinUseCase;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.springframework.stereotype.Service;

@Service
public class PinTranslationService implements TranslatePinUseCase {

    private final PaymentHsmPort hsm;

    public PinTranslationService(PaymentHsmPort hsm) {
        this.hsm = hsm;
    }

    @Override
    public PinBlock translateFromTpk(EncryptedKey sourceTpk, EncryptedKey destinationZpk,
                                      PinBlock sourcePinBlock, Pan pan) {
        return hsm.translatePinFromTpkToZpk(sourceTpk, destinationZpk, sourcePinBlock, pan);
    }

    @Override
    public PinBlock translateFromDukpt(EncryptedKey sourceBdk, EncryptedKey destinationZpk,
                                        Ksn ksn, PinBlock sourcePinBlock, Pan pan) {
        return hsm.translatePinFromDukptToZpk(sourceBdk, destinationZpk, ksn, sourcePinBlock, pan);
    }
}
