package com.hsmclient.api;

import com.hsmclient.api.dto.PinBlockResponse;
import com.hsmclient.api.dto.TranslatePinDukptRequest;
import com.hsmclient.api.dto.TranslatePinTpkRequest;
import com.hsmclient.domain.model.*;
import com.hsmclient.domain.port.in.TranslatePinUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ejemplo de vertical slice completo: HTTP -> caso de uso -> puerto -> HSM.
 * Equivalente al flujo de PINTranslate.java del cliente jPOS original, pero como
 * endpoint HTTP independiente en vez de una transaccion embebida en el switch.
 */
@RestController
@RequestMapping("/api/v1/pin")
public class PinTranslationController {

    private final TranslatePinUseCase translatePin;

    public PinTranslationController(TranslatePinUseCase translatePin) {
        this.translatePin = translatePin;
    }

    @PostMapping("/translate/tpk")
    public PinBlockResponse translateFromTpk(@Valid @RequestBody TranslatePinTpkRequest request) {
        PinBlock result = translatePin.translateFromTpk(
                EncryptedKey.of(request.sourceTpk()),
                EncryptedKey.of(request.destinationZpk()),
                PinBlock.iso0(request.sourcePinBlock()),
                new Pan(request.pan()));
        return PinBlockResponse.from(result);
    }

    @PostMapping("/translate/dukpt")
    public PinBlockResponse translateFromDukpt(@Valid @RequestBody TranslatePinDukptRequest request) {
        PinBlock result = translatePin.translateFromDukpt(
                EncryptedKey.of(request.sourceBdk()),
                EncryptedKey.of(request.destinationZpk()),
                new Ksn(request.ksnDescriptor(), request.ksn()),
                PinBlock.iso0(request.sourcePinBlock()),
                new Pan(request.pan()));
        return PinBlockResponse.from(result);
    }
}
