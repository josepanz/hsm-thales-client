package com.hsmclient.domain.port.in;

import com.hsmclient.domain.model.*;

public interface GeneratePinOffsetUseCase {

    /** Comando Thales BK. */
    PinOffset generate(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                        DecimalizationTable decimalizationTable, PinVerificationData validationData);
}
