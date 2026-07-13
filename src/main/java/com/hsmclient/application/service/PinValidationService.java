package com.hsmclient.application.service;

import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.in.GeneratePinOffsetUseCase;
import com.hsmclient.domain.port.in.ValidatePinUseCase;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.springframework.stereotype.Service;

@Service
public class PinValidationService implements ValidatePinUseCase, GeneratePinOffsetUseCase {

    private final PaymentHsmPort hsm;

    public PinValidationService(PaymentHsmPort hsm) {
        this.hsm = hsm;
    }

    @Override
    public void validate(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                          DecimalizationTable decimalizationTable, PinVerificationData validationData,
                          PinOffset expectedOffset) {
        hsm.validatePinOffset(zpk, pvk, pinBlock, pan, decimalizationTable, validationData, expectedOffset);
    }

    @Override
    public PinOffset generate(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                               DecimalizationTable decimalizationTable, PinVerificationData validationData) {
        return hsm.generatePinOffset(zpk, pvk, pinBlock, pan, decimalizationTable, validationData);
    }
}
