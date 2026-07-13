package com.hsmclient.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslatePinTpkRequest(
        @NotBlank String sourceTpk,
        @NotBlank String destinationZpk,
        @NotBlank String sourcePinBlock,
        @NotBlank String pan) {
}
