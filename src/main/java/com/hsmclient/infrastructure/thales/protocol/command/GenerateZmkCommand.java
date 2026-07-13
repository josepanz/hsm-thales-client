package com.hsmclient.infrastructure.thales.protocol.command;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.GeneratedKey;
import com.hsmclient.domain.model.KeyScheme;
import com.hsmclient.infrastructure.thales.protocol.ThalesCommand;
import com.hsmclient.infrastructure.thales.protocol.ThalesResponses;
import com.hsmclient.infrastructure.thales.protocol.codec.FieldCodec;

import java.util.Optional;

/**
 * Comando A0 - Generate a Key. Tiene dos modos reales segun si se provee una ZMK
 * "envolvente" o no:
 * <ul>
 *   <li>Modo 0: genera una llave y la devuelve cifrada solo bajo el LMK del HSM.
 *       Es el unico modo ejercitado por los tests originales (ThalesManagerTest#testGenerateKey,
 *       invocado con mode="ZMK") y por lo tanto el unico validado contra un HSM real.</li>
 *   <li>Modo 1: genera una llave y la devuelve tambien envuelta bajo una ZMK de
 *       transporte que se provee en el request (para intercambiarla con un tercero).</li>
 * </ul>
 * <b>Nota de honestidad tecnica:</b> el recurso XML original para el modo 1
 * (hsm-resp-A1ZMK.xml, a pesar del nombre, es el que en realidad corresponde al modo 0 -
 * ver ThalesAdapter.createRequest, que arma el sufijo como comando+version) redeclaraba
 * los campos "response"/"code" que ya consume el schema base, lo cual habria desalineado
 * el parseo en 4 bytes. Esta reimplementacion NO repite ese bug: el response-code y el
 * error-code siempre se extraen una sola vez en la capa generica (ver
 * {@code ThalesConnection}) antes de llegar aca. El layout de la respuesta del modo 1
 * (key-scheme + key de 48 hex + check value) se dejo segun la intencion del schema
 * original, pero no fue ejercitado por ningun test existente - valida contra el manual
 * de tu firmware exacto antes de usarlo en produccion.
 */
public record GenerateZmkCommand(KeyScheme scheme, Optional<EncryptedKey> wrappingZmk)
        implements ThalesCommand<GeneratedKey> {

    public static GenerateZmkCommand underLmk(KeyScheme scheme) {
        return new GenerateZmkCommand(scheme, Optional.empty());
    }

    public static GenerateZmkCommand underZmk(EncryptedKey wrappingZmk) {
        return new GenerateZmkCommand(KeyScheme.DOUBLE_LENGTH, Optional.of(wrappingZmk));
    }

    @Override
    public String requestCode() {
        return "A0";
    }

    @Override
    public String encodeBody() {
        if (wrappingZmk.isPresent()) {
            return "1" + "001" + "U"
                    + FieldCodec.fixed(wrappingZmk.get().hexValue(), 32, "zmk")
                    + "U";
        }
        return "0" + "000" + "U";
    }

    @Override
    public GeneratedKey parseResponse(String responseCode, String errorCode, String remainingFields) {
        ThalesResponses.requireSuccess(errorCode);
        if (wrappingZmk.isPresent()) {
            char schemeFlag = remainingFields.charAt(0);
            String key = remainingFields.substring(1, 49);
            String checkValue = remainingFields.substring(49, 55);
            KeyScheme resultScheme = schemeFlag == 'T' ? KeyScheme.TRIPLE_LENGTH : KeyScheme.DOUBLE_LENGTH;
            return new GeneratedKey(new EncryptedKey(key, resultScheme), checkValue);
        }
        String key = remainingFields.substring(0, 32);
        String checkValue = remainingFields.substring(32, 38);
        return new GeneratedKey(EncryptedKey.of(key), checkValue);
    }
}
