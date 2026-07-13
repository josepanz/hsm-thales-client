package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.RsaKeyPair;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;
import com.hsmclient.infrastructure.thales.protocol.codec.Hex;

/**
 * Comando EI - Generate an RSA Key Pair.
 * <p>
 * <b>Nota de honestidad tecnica:</b> el request esta verificado contra el ejemplo real
 * documentado en el recurso original ({@code "000000000001EI0204801"} => header + "EI" +
 * key-type-indicator "0" + key-length "2048" + encoding "01"). La respuesta, en cambio,
 * NO tiene ningun test que la ejercite en el codigo original (el unico test existente
 * esta con @Ignore y no hace asserts sobre la estructura), y el schema original
 * (hsm-resp-EJ.xml) declara la llave publica en solo 32 hex chars - insuficiente para el
 * modulo de una llave RSA-2048 real. Por eso aca se parsea el largo de la llave privada
 * de forma explicita (length-prefixed) en vez de asumir un tamano fijo, pero de todas
 * formas: validar el layout exacto contra el manual de tu firmware antes de usarlo en
 * produccion, este comando se deja como mejor esfuerzo.
 */
public record GenerateRsaKeyPairCommand(int keyLengthBits) implements ThalesCommand<RsaKeyPair> {

    private static final int PUBLIC_KEY_HEX_LENGTH = 32;
    private static final int LENGTH_FIELD_HEX_LENGTH = 4;

    @Override
    public String requestCode() {
        return "EI";
    }

    @Override
    public String encodeBody() {
        return "0"
                + FieldCodec.numeric(keyLengthBits, 4)
                + "01";
    }

    @Override
    public RsaKeyPair parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        String publicKeyHex = remainingFields.substring(0, PUBLIC_KEY_HEX_LENGTH);
        int cursor = PUBLIC_KEY_HEX_LENGTH;
        int privateKeyBytes = Integer.parseInt(remainingFields.substring(cursor, cursor + LENGTH_FIELD_HEX_LENGTH));
        cursor += LENGTH_FIELD_HEX_LENGTH;
        String privateKeyHex = remainingFields.substring(cursor, cursor + privateKeyBytes * 2);
        return new RsaKeyPair(Hex.decode(publicKeyHex), Hex.decode(privateKeyHex));
    }
}
