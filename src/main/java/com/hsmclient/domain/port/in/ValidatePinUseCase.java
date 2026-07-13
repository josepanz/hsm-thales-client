package com.hsmclient.domain.port.in;

import com.hsmclient.domain.model.*;

public interface ValidatePinUseCase {

    /** Comando Thales EA. Lanza HsmException si el offset no coincide. */
    void validate(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                  DecimalizationTable decimalizationTable, PinVerificationData validationData,
                  PinOffset expectedOffset);
}
