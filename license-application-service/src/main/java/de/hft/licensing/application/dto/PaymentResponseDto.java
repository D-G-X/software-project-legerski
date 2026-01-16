package de.hft.licensing.application.dto;

public record PaymentResponseDto(
        String application_id,
        String payment_id,
        String status,
        String rejection_reason
) {
}
