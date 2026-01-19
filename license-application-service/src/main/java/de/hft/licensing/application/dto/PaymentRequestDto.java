package de.hft.licensing.application.dto;

public record PaymentRequestDto(
        String application_id,
        double amount,
        String name,
        String iban,
        String bic
) {
}
