package com.hsmclient.application.service;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.out.PaymentHsmPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PinValidationServiceTest {

    @Mock
    private PaymentHsmPort hsm;

    private final EncryptedKey zpk = EncryptedKey.of("AABBCCDDEEFF00112233445566778899");
    private final EncryptedKey pvk = EncryptedKey.of("54E262D26A1F1C32");
    private final PinBlock pinBlock = PinBlock.iso0("EB10B1CD37EE75E2");
    private final Pan pan = new Pan("002008986420");
    private final DecimalizationTable table = new DecimalizationTable("FD64FC3FAD504BB3");
    private final PinVerificationData validationData = new PinVerificationData("0020089N0000");
    private final PinOffset offset = new PinOffset("1750");

    @Test
    void validatePropagatesSuccessSilently() {
        PinValidationService service = new PinValidationService(hsm);
        service.validate(zpk, pvk, pinBlock, pan, table, validationData, offset);
        // no exception -> offset valido
    }

    @Test
    void validatePropagatesHsmExceptionOnMismatch() {
        doThrow(new HsmException("01")).when(hsm).validatePinOffset(zpk, pvk, pinBlock, pan, table, validationData, offset);

        PinValidationService service = new PinValidationService(hsm);

        assertThatThrownBy(() -> service.validate(zpk, pvk, pinBlock, pan, table, validationData, offset))
                .isInstanceOf(HsmException.class);
    }

    @Test
    void generateDelegatesToThePort() {
        when(hsm.generatePinOffset(zpk, pvk, pinBlock, pan, table, validationData)).thenReturn(offset);

        PinValidationService service = new PinValidationService(hsm);
        PinOffset result = service.generate(zpk, pvk, pinBlock, pan, table, validationData);

        assertThat(result).isEqualTo(offset);
    }
}
