package de.hft.licensing.services;

import de.hft.licensing.utils.auth.LoginRequest;
import de.hft.licensing.utils.auth.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KeycloakAuthService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public LoginResponse login(LoginRequest request) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("username", request.getUsername());
        params.put("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        StringBuilder body = new StringBuilder();
        params.forEach((k, v) -> body.append(k).append("=").append(v).append("&"));
        body.setLength(body.length() - 1);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<LoginResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, LoginResponse.class);

        return response.getBody();
    }
}