package com.hsmclient.domain.port.out;

import com.hsmclient.domain.model.*;

/**
 * Puerto de salida hacia el HSM de pagos (Thales payShield 9000/10000 en modo Variant LMK).
 * La capa de aplicacion solo conoce esta interfaz; el protocolo de red concreto vive en
 * infrastructure. Esto permite, por ejemplo, testear los casos de uso sin sockets reales.
 */
public interface PaymentHsmPort {

    PinBlock translatePinFromTpkToZpk(EncryptedKey sourceTpk, EncryptedKey destinationZpk,
                                       PinBlock sourcePinBlock, Pan pan);

    PinBlock translatePinFromDukptToZpk(EncryptedKey sourceBdk, EncryptedKey destinationZpk,
                                         Ksn ksn, PinBlock sourcePinBlock, Pan pan);

    void validatePinOffset(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                            DecimalizationTable decimalizationTable, PinVerificationData validationData,
                            PinOffset expectedOffset);

    PinOffset generatePinOffset(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                                 DecimalizationTable decimalizationTable, PinVerificationData validationData);

    GeneratedKey generateZmk(KeyScheme scheme);

    GeneratedKey generateZmkUnderZmk(EncryptedKey wrappingZmk);

    RsaKeyPair generateRsaKeyPair(int keyLengthBits);

    HsmHealth diagnostics();

    HostStatus hostStatus(HostStatusMode mode);
}
