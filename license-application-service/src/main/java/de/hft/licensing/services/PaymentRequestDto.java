package de.hft.licensing.services;

public record PaymentRequestDto(
        String application_id,
        double amount,
        String name,
        String iban,
        String bic
) {
}
