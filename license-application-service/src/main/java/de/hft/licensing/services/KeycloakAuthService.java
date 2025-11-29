package de.hft.licensing.services;

import de.hft.licensing.db.tables.User;
import de.hft.licensing.model.LoginRequest;
import de.hft.licensing.model.LoginResource;
import de.hft.licensing.model.RegisterRequest;
import de.hft.licensing.model.RegisterResource;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KeycloakAuthService {

    private final DSLContext dsl;

    public KeycloakAuthService(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public LoginResource login(LoginRequest request) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("username", request.getEmail());
        params.put("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<LoginResource> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, LoginResource.class);

        return response.getBody();
    }

    public RegisterResource register(RegisterRequest request) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("email", request.getEmail());
        user.put("username", request.getFirstname() + request.getLastname() + request.getEmail());
        user.put("firstName", request.getFirstname());
        user.put("lastName", request.getLastname());
        user.put("enabled", true);
        user.put("emailVerified", false);

        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("type", "password");
        credentials.put("value", request.getPassword());
        credentials.put("temporary", false);

        user.put("credentials", new Map[]{credentials});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        UUID newUserUUID = extractUserUUIdFromLocationHeader(response);

        RegisterResource registerResource = new RegisterResource();
        try {
            assert newUserUUID != null;
            int inserted = dsl.insertInto(User.USER)
                    .set(User.USER.ID, newUserUUID.toString())
                    .execute();
            if (inserted == 0){
                System.out.println("[WARNING] - Failed to insert user with ID " + newUserUUID + " into the local database.");
                registerResource.setUserId(null);
                registerResource.setMessage("Failed to register user");
            } else {
                registerResource.setUserId(newUserUUID);
                registerResource.setMessage("User registered successfully");
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println("[ERROR] - User with ID " + newUserUUID + " already exists in the local database.");
            registerResource.setUserId(null);
            registerResource.setMessage("User already exists");
        }
        return registerResource;
    }

    private UUID extractUserUUIdFromLocationHeader(ResponseEntity<String> response) {
        String location = Objects.requireNonNull(response.getHeaders().get("Location")).getFirst();
        if (location != null && location.contains("/users/")) {
            return UUID.fromString(location.substring(location.lastIndexOf("/") + 1));
        }
        return null;
    }

    private String getAdminToken() {
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", "admin-cli");
        params.put("username", "admin");
        params.put("password", "admin");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        StringBuilder body = new StringBuilder();
        params.forEach((k, v) -> body.append(k).append("=").append(v).append("&"));
        body.setLength(body.length() - 1);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map responseBody = response.getBody();
        return responseBody != null ? (String) responseBody.get("access_token") : null;
    }
}