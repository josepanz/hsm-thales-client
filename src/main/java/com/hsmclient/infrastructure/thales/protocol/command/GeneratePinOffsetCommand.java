package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.*;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;

/** Comando BK - Generate a PIN Offset (inverso de EA: dado un PIN valido, calcula su offset). */
public record GeneratePinOffsetCommand(EncryptedKey zpk, EncryptedKey pvk, PinBlock pinBlock, Pan pan,
                                         DecimalizationTable decimalizationTable,
                                         PinVerificationData validationData)
        implements ThalesCommand<PinOffset> {

    @Override
    public String requestCode() {
        return "BK";
    }

    @Override
    public String encodeBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("001");                                       // pinblock-key-type: ZPK
        sb.append("U").append(FieldCodec.fixed(zpk.hexValue(), 32, "pinblock-key"));
        if (pvk.scheme().isExplicit()) {
            sb.append(pvk.scheme().flag());
        }
        sb.append(pvk.hexValue());
        sb.append(FieldCodec.fixed(pinBlock.hexValue(), 16, "pinblock"));
        sb.append(pinBlock.format().code());
        sb.append("04");                                        // pinblock-checklen
        sb.append(FieldCodec.fixed(pan.last12(), 12, "pan"));
        sb.append(FieldCodec.fixed(decimalizationTable.hexValue(), 16, "decimalization-table"));
        sb.append(FieldCodec.fixed(validationData.value(), 12, "validation-data"));
        return sb.toString();
    }

    @Override
    public PinOffset parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        return new PinOffset(remainingFields.substring(0, 12));
    }
}
