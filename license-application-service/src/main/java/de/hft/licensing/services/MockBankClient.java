package de.hft.licensing.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MockBankClient {
    private final WebClient webClient;

    public MockBankClient(@Value("${mockbank.base-url}") String baseUrl, WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public PaymentResponseDto processPayment(PaymentRequestDto req) {
        return webClient.post()
                .uri("/process-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(PaymentResponseDto.class)
                .block();
    }
}
