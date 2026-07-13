package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.*;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;

/**
 * Comando G0 - Translate PIN block from BDK/DUKPT to ZPK. Terminales DUKPT (ej. Ingenico):
 * la llave de trabajo no es fija, se deriva del BDK + KSN en cada transaccion.
 */
public record TranslatePinDukptCommand(EncryptedKey sourceBdk, EncryptedKey destinationZpk,
                                         Ksn ksn, PinBlock sourcePinBlock, Pan pan)
        implements ThalesCommand<PinBlock> {

    @Override
    public String requestCode() {
        return "G0";
    }

    @Override
    public String encodeBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("~");                                    // source-key-flag: BDK
        sb.append("U").append(FieldCodec.fixed(sourceBdk.hexValue(), 32, "source-key"));
        sb.append("U").append(FieldCodec.fixed(destinationZpk.hexValue(), 32, "destination-key"));
        sb.append(FieldCodec.fixed(ksn.descriptor(), 3, "ksn-descriptor"));
        sb.append(FieldCodec.fixed(ksn.value(), 20, "ksn"));
        sb.append(FieldCodec.fixed(sourcePinBlock.hexValue(), 16, "source-pinblk"));
        sb.append(sourcePinBlock.format().code());
        sb.append(PinBlockFormat.ISO_0.code());
        sb.append(FieldCodec.fixed(pan.last12(), 12, "pan"));
        return sb.toString();
    }

    @Override
    public PinBlock parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        // remainingFields: destination-pinblk-length(2) + destination-pinblk(16)
        String destinationPinBlock = remainingFields.substring(2, 18);
        return PinBlock.iso0(destinationPinBlock);
    }
}
