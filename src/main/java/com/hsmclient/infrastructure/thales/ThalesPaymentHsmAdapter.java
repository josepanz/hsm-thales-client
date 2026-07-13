package com.hsmclient.infrastructure.thales;

import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import com.hsmclient.infrastructure.thales.protocol.command.*;
import com.hsmclient.infrastructure.thales.transport.ThalesConnectionPool;

import java.time.Duration;

/**
 * Adapter que implementa el puerto de dominio {@link PaymentHsmPort} hablando el
 * protocolo Thales Variant LMK (payShield 9000/10000) sobre el pool de conexiones TCP.
 * Es intencionalmente un traductor fino: domain <-> ThalesCommand. Toda la logica de
 * protocolo vive en los comandos; toda la logica de red vive en el pool/conexion.
 */
public class ThalesPaymentHsmAdapter implements PaymentHsmPort {

    private final ThalesConnectionPool pool;
    private final Duration requestTimeout;

    public ThalesPaymentHsmAdapter(ThalesConnectionPool pool, Duration requestTimeout) {
        this.pool = pool;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public PinBlock translatePinFromTpkToZpk(EncryptedKey sourceTpk, EncryptedKey destinationZpk,
                                              PinBlock sourcePinBlock, Pan pan) {
        return execute(new TranslatePinTpkToZpkCommand(sourceTpk, destinationZpk, sourcePinBlock, pan));
    }

    @Override
    public PinBlock translatePinFromDukptToZpk(EncryptedKey sourceBdk, EncryptedKey destinationZpk,
                                                Ksn ksn, PinBlock sourcePinBlock, Pan pan) {
        return execute(new TranslatePinDukptCommand(sourceBdk, destinationZpk, ksn, sourcePinBlock, pan));
    }

    @Override
    public void validatePinOffset(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                                   DecimalizationTable decimalizationTable, PinVerificationData validationData,
                                   PinOffset expectedOffset) {
        execute(new ValidatePinOffsetCommand(zpk, pvk, pinBlock, pan, decimalizationTable, validationData, expectedOffset));
    }

    @Override
    public PinOffset generatePinOffset(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                                        DecimalizationTable decimalizationTable, PinVerificationData validationData) {
        return execute(new GeneratePinOffsetCommand(zpk, pvk, pinBlock, pan, decimalizationTable, validationData));
    }

    @Override
    public GeneratedKey generateZmk(KeyScheme scheme) {
        return execute(GenerateZmkCommand.underLmk(scheme));
    }

    @Override
    public GeneratedKey generateZmkUnderZmk(EncryptedKey wrappingZmk) {
        return execute(GenerateZmkCommand.underZmk(wrappingZmk));
    }

    @Override
    public RsaKeyPair generateRsaKeyPair(int keyLengthBits) {
        return execute(new GenerateRsaKeyPairCommand(keyLengthBits));
    }

    @Override
    public HsmHealth diagnostics() {
        return execute(new DiagnosticsCommand());
    }

    @Override
    public HostStatus hostStatus(HostStatusMode mode) {
        return execute(new HostStatusCommand(mode));
    }

    private <R> R execute(com.hsmclient.infrastructure.thales.protocol.ThalesCommand<R> command) {
        return pool.acquire().send(command, requestTimeout);
    }
}
