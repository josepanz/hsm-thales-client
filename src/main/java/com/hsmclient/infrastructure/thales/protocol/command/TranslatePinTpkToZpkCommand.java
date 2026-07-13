package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.Pan;
import com.hsmclient.domain.model.PinBlock;
import com.hsmclient.domain.model.PinBlockFormat;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;

import java.util.Map;

/**
 * Comando CA - Translate PIN from TPK to ZPK. Terminales con llave de trabajo fija
 * (TPK/ZPK), tipicamente ATM y POS Verifone.
 * <p>
 * Formato exacto verificado contra hsm-CA16.xml / hsm-CA32.xml del cliente original:
 * el flag de esquema de la llave destino solo se envia cuando esta es de doble/triple
 * longitud (ver {@link com.hsmclient.domain.model.KeyScheme#isExplicit()}); para una
 * llave simple longitud no se declara, que es exactamente la asimetria que tenian los
 * dos XML separados (CA16 sin "destination-key-type", CA32 con el campo).
 */
public record TranslatePinTpkToZpkCommand(EncryptedKey sourceTpk, EncryptedKey destinationZpk,
                                            PinBlock sourcePinBlock, Pan pan)
        implements ThalesCommand<PinBlock> {

    private static final Map<String, PinBlockFormat> FORMAT_BY_CODE = Map.of(
            PinBlockFormat.ISO_0.code(), PinBlockFormat.ISO_0,
            PinBlockFormat.ISO_1.code(), PinBlockFormat.ISO_1,
            PinBlockFormat.ISO_2.code(), PinBlockFormat.ISO_2,
            PinBlockFormat.ISO_3.code(), PinBlockFormat.ISO_3);

    @Override
    public String requestCode() {
        return "CA";
    }

    @Override
    public String encodeBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("U");                                  // source-tpk-type: siempre doble longitud
        sb.append(FieldCodec.fixed(sourceTpk.hexValue(), 32, "source-tpk"));
        if (destinationZpk.scheme().isExplicit()) {
            sb.append(destinationZpk.scheme().flag());    // destination-key-type, solo si aplica
        }
        sb.append(destinationZpk.hexValue());
        sb.append("12");                                  // max-pin-length
        sb.append(FieldCodec.fixed(sourcePinBlock.hexValue(), 16, "source-pinblk"));
        sb.append(sourcePinBlock.format().code());        // source-pinblk-format
        sb.append(PinBlockFormat.ISO_0.code());           // destination-pinblk-format
        sb.append(FieldCodec.fixed(pan.last12(), 12, "pan"));
        return sb.toString();
    }

    @Override
    public PinBlock parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        // remainingFields: pin-length(2) + destination-pinblk(16) + destination-pinblk-format(2)
        String destinationPinBlock = remainingFields.substring(2, 18);
        String formatCode = remainingFields.substring(18, 20);
        return new PinBlock(destinationPinBlock, FORMAT_BY_CODE.getOrDefault(formatCode, PinBlockFormat.ISO_0));
    }
}
