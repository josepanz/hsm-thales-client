package com.hsmclient.domain.port.in;

import com.hsmclient.domain.model.EncryptedKey;
import com.hsmclient.domain.model.GeneratedKey;
import com.hsmclient.domain.model.KeyScheme;
import com.hsmclient.domain.model.RsaKeyPair;

public interface KeyManagementUseCase {

    /** Comando Thales A0 (bajo LMK). */
    GeneratedKey generateZmk(KeyScheme scheme);

    /** Comando Thales A0 variante ZMK (bajo otro ZMK). */
    GeneratedKey generateZmkUnderZmk(EncryptedKey wrappingZmk);

    /** Comando Thales EI. */
    RsaKeyPair generateRsaKeyPair(int keyLengthBits);
}
