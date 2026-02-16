package de.hft.licensing.application.services;

import de.hft.licensing.application.dto.PaymentRequestDto;
import de.hft.licensing.application.dto.PaymentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockBankServiceTest {

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private MockBankService service;

    @BeforeEach
    void setUp() {
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        service = new MockBankService("http://mockbank", builder);
    }

    @Test
    void processPayment_buildsRequestChain_andReturnsResponse() {
        PaymentRequestDto req = mock(PaymentRequestDto.class);
        PaymentResponseDto resp = new PaymentResponseDto("1", "1", "approved", "any");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/process-payment")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);

        when(requestBodySpec.bodyValue(req)).thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PaymentResponseDto.class)).thenReturn(Mono.just(resp));

        PaymentResponseDto out = service.processPayment(req);

        assertSame(resp, out);

        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/process-payment");
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).bodyValue(req);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(PaymentResponseDto.class);
    }
}