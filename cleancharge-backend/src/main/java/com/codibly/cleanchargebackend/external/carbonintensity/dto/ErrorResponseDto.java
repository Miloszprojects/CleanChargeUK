package com.codibly.cleanchargebackend.external.carbonintensity.dto;

public record ErrorResponseDto(
        ErrorDetail error
) {
    public record ErrorDetail(String code, String message) {}
}
