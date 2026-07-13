package com.hsmclient.api.dto;

import com.hsmclient.domain.model.PinBlock;

public record PinBlockResponse(String pinBlock, String format) {

    public static PinBlockResponse from(PinBlock pinBlock) {
        return new PinBlockResponse(pinBlock.hexValue(), pinBlock.format().name());
    }
}
