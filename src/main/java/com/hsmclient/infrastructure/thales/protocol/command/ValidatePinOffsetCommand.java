package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.*;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;

/**
 * Comando EA - PIN Offset Validation (algoritmo IBM 3624).
 * <p>
 * Correccion respecto al original: hsm-EA32.xml declaraba el campo "zpk-type" dos veces
 * (una vez para el ZPK y, por copy/paste, otra vez en el lugar donde debia decir
 * "pvk-type" para el PVK). Al no haber tipos ni validacion de longitud, ese bug no
 * rompia el pack (el valor literal "U" es igual en ambos casos) pero era enganoso y
 * fragil frente a cualquier cambio futuro. Aca el flag de cada llave se deriva de su
 * propio {@link KeyScheme}, no hay campos hardcodeados con nombre incorrecto posible.
 */
public record ValidatePinOffsetCommand(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                                         DecimalizationTable decimalizationTable,
                                         PinVerificationData validationData, PinOffset expectedOffset)
        implements ThalesCommand<Void> {

    @Override
    public String requestCode() {
        return "EA";
    }

    @Override
    public String encodeBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("U").append(FieldCodec.fixed(zpk.hexValue(), 32, "zpk"));
        if (pvk.scheme().isExplicit()) {
            sb.append(pvk.scheme().flag());
        }
        sb.append(pvk.hexValue());
        sb.append("12");                                        // max-pin-length
        sb.append(FieldCodec.fixed(pinBlock.hexValue(), 16, "pinblk"));
        sb.append(pinBlock.format().code());
        sb.append("04");                                        // pin-minlen-check
        sb.append(FieldCodec.fixed(pan.last12(), 12, "pan"));
        sb.append(FieldCodec.fixed(decimalizationTable.hexValue(), 16, "decimalization-table"));
        sb.append(FieldCodec.fixed(validationData.value(), 12, "validation-data"));
        sb.append(expectedOffset.padded());
        return sb.toString();
    }

    @Override
    public Void parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        return null;
    }
}
