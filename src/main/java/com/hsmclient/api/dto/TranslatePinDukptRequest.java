package com.hsmclient.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslatePinDukptRequest(
        @NotBlank String sourceBdk,
        @NotBlank String destinationZpk,
        @NotBlank String ksnDescriptor,
        @NotBlank String ksn,
        @NotBlank String sourcePinBlock,
        @NotBlank String pan) {
}
