package de.hft.licensing.services;

public record PaymentResponseDto(
        String application_id,
        String payment_id,
        String status,
        String rejection_reason
) {
}
