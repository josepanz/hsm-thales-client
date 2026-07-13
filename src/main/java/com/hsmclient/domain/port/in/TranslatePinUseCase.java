package com.hsmclient.domain.port.in;

import com.hsmclient.domain.model.*;

public interface TranslatePinUseCase {

    /** Terminales con llave fija (TPK/ZPK), ej. ATM, Verifone. Comando Thales CA. */
    PinBlock translateFromTpk(EncryptedKey sourceTpk, EncryptedKey destinationZpk,
                               PinBlock sourcePinBlock, Pan pan);

    /** Terminales DUKPT (BDK derivado por KSN), ej. Ingenico. Comando Thales G0. */
    PinBlock translateFromDukpt(EncryptedKey sourceBdk, EncryptedKey destinationZpk,
                                 Ksn ksn, PinBlock sourcePinBlock, Pan pan);
}
