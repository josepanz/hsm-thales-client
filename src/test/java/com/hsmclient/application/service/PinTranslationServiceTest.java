package com.hsmclient.application.service;

import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El servicio de aplicacion no debe tener logica propia mas alla de delegar al puerto:
 * si un dia agrega logica (ej. logging, metricas), este test sigue siendo la
 * especificacion de que el caso de uso llama al puerto con exactamente esos argumentos.
 */
@ExtendWith(MockitoExtension.class)
class PinTranslationServiceTest {

    @Mock
    private PaymentHsmPort hsm;

    @Test
    void translateFromTpkDelegatesToThePort() {
        EncryptedKey sourceTpk = EncryptedKey.of("8699395CD2688A2CEFD97DAEF3BD4D61");
        EncryptedKey destinationZpk = EncryptedKey.of("D3A8A50F20BD9B00");
        PinBlock sourcePinBlock = PinBlock.iso0("8EF09CD0A28D7DB0");
        Pan pan = new Pan("976000000001");
        PinBlock expected = PinBlock.iso0("1122334455667788");

        when(hsm.translatePinFromTpkToZpk(sourceTpk, destinationZpk, sourcePinBlock, pan)).thenReturn(expected);

        PinTranslationService service = new PinTranslationService(hsm);
        PinBlock result = service.translateFromTpk(sourceTpk, destinationZpk, sourcePinBlock, pan);

        assertThat(result).isEqualTo(expected);
        verify(hsm).translatePinFromTpkToZpk(sourceTpk, destinationZpk, sourcePinBlock, pan);
    }

    @Test
    void translateFromDukptDelegatesToThePort() {
        EncryptedKey sourceBdk = EncryptedKey.of("E56448F66FDC9B57ECB87469CB998830");
        EncryptedKey destinationZpk = EncryptedKey.of("DEF7DBE03A6BF1FB2EB41B5F57892F07");
        Ksn ksn = new Ksn("609", "FFFF0000100000200674");
        PinBlock sourcePinBlock = PinBlock.iso0("8A56466ADDCAEA1B");
        Pan pan = new Pan("625960050010");
        PinBlock expected = PinBlock.iso0("8877665544332211");

        when(hsm.translatePinFromDukptToZpk(sourceBdk, destinationZpk, ksn, sourcePinBlock, pan)).thenReturn(expected);

        PinTranslationService service = new PinTranslationService(hsm);
        PinBlock result = service.translateFromDukpt(sourceBdk, destinationZpk, ksn, sourcePinBlock, pan);

        assertThat(result).isEqualTo(expected);
    }
}
