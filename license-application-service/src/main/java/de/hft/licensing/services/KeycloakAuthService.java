package de.hft.licensing.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        LoginResource loginResource = response.getBody();

        // determine is_admin by decoding access_token (JWT) payload
        if (loginResource != null && loginResource.getAccessToken() != null) {
            boolean isAdmin = tokenHasAdminRole(loginResource.getAccessToken());
            loginResource.setIsAdmin(isAdmin);
        }

        return loginResource;
    }


    public LoginResource refreshLogin(RefreshLoginRequest refreshLoginRequest) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("refresh_token", refreshLoginRequest.getRefreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<LoginResource> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, LoginResource.class);

        LoginResource loginResource = response.getBody();

        if (loginResource != null && loginResource.getAccessToken() != null) {
            boolean isAdmin = tokenHasAdminRole(loginResource.getAccessToken());
            loginResource.setIsAdmin(isAdmin);
        }

        return loginResource;
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
            if (inserted == 0) {
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

        // Create user's Notification Preferences record with default values
        try {
            int insertedPreferences = dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID, newUserUUID.toString())
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY, (NotificationWay) EnumMapperUtil.getPendantFromEnum(NotificationWayApiEnum.NONE))
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION, true)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION, true)
                .execute();
            if (insertedPreferences == 0) {
                System.out.println("[WARNING] - Failed to insert notification preferences for user ID " + newUserUUID + " into the local database.");
            } else {
                System.out.println("[INFO] - Notification preferences for user ID " + newUserUUID + " created successfully in the local database.");
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
            System.out.println("[ERROR] - Notification preferences for user ID " + newUserUUID + " already exist in the local database.");
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

    public KeycloakUserRecord getUserById(UUID userId) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            return null;
        }

        String url = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KeycloakUserRecord> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakUserRecord.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return null;
        }

        return response.getBody();
    }
    public void updateUser(UUID userId, UpdateUserRequest request) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, userId);

        String firstName  = request.getFirstName();
        String lastName   = request.getLastName();
        String email      = request.getEmail();
        Boolean enabled   = request.getEnabled();

        List<KeycloakCredentialRecord> credentials = null;
        if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
            credentials = request.getCredentials().stream()
                    .map(this::mapCredential)
                    .toList();
        }

        KeycloakUserUpdateRecord payload =
                new KeycloakUserUpdateRecord(firstName, lastName, email, enabled, credentials);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<KeycloakUserUpdateRecord> entity = new HttpEntity<>(payload, headers);

        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public boolean deleteUserInKeycloak(UUID userId) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return true;
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return false;
            }
            throw new RuntimeException("Failed to delete user in Keycloak: " + e.getMessage(), e);
        }
    }

    private KeycloakCredentialRecord mapCredential(UpdateUserRequestCredentialsInner src) {
        return new KeycloakCredentialRecord(
                src.getType().getValue(),
                src.getValue(),
                Boolean.TRUE.equals(src.getTemporary())
        );
    }

    private boolean tokenHasAdminRole(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) return false;
            String payloadB64 = parts[1];
            int padding = (4 - (payloadB64.length() % 4)) % 4;
            payloadB64 += "=".repeat(padding);
            byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
            JsonNode payload = objectMapper.readTree(decoded);

            // check realm_access.roles
            JsonNode realmAccess = payload.get("realm_access");
            if (realmAccess != null && realmAccess.has("roles")) {
                for (JsonNode roleNode : realmAccess.get("roles")) {
                    if ("admin".equalsIgnoreCase(roleNode.asText())) return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }


    public record KeycloakUserRecord(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            @JsonProperty("emailVerified") boolean emailVerified,
            @JsonProperty("createdTimestamp") Long createdTimestamp
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KeycloakUserUpdateRecord(
            String firstName,
            String lastName,
            String email,
            Boolean enabled,
            List<KeycloakCredentialRecord> credentials
    ) {}

    public record KeycloakCredentialRecord(
            String type,
            String value,
            boolean temporary
    ) {}
}