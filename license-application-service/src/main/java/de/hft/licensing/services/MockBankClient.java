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

    /**
     * Calls the mock bank service to process a payment.
     *
     * @param req the payment request data
     * @return the payment response data
     */
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
