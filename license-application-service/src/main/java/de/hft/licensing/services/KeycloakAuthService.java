package de.hft.licensing.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.PasswordResetToken;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String adminToken = getAdminToken();
        if (adminToken == null) throw new RuntimeException("Failed to obtain admin token from Keycloak");
        headers.setBearerAuth(adminToken);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("email", request.getEmail());
        user.put("username", request.getEmail());
        user.put("firstName", request.getFirstname());
        user.put("lastName", request.getLastname());
        user.put("enabled", true);
        user.put("emailVerified", true);

        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("type", "password");
        credentials.put("value", request.getPassword());
        credentials.put("temporary", false);
        user.put("credentials", new Map[]{credentials});

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(user, headers), String.class);

            UUID id = extractUserUUIdFromLocationHeader(response);
            ensureLocalUserAndDefaults(id);

            RegisterResource rr = new RegisterResource();
            rr.setUserId(id);
            rr.setMessage("User registered successfully");
            return rr;

        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            UUID existingId = getUserIdByEmail(request.getEmail());
            if (existingId == null) throw new RuntimeException("User exists in Keycloak but could not be fetched by email", e);

            ensureLocalUserAndDefaults(existingId);

            RegisterResource rr = new RegisterResource();
            rr.setUserId(existingId);
            rr.setMessage("User already exists");
            return rr;
        }
    }

    private void ensureLocalUserAndDefaults(UUID userId) {
        try {
            dsl.insertInto(User.USER)
                    .set(User.USER.ID, userId.toString())
                    .execute();
        } catch (DataIntegrityViolationException ignored) {}

        try {
            dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID, userId.toString())
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY,
                            (NotificationWay) EnumMapperUtil.getPendantFromEnum(NotificationWayApiEnum.NONE))
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION, true)
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION, true)
                    .execute();
        } catch (DataIntegrityViolationException ignored) {}
    }

    public boolean isEmailRegistered(String email) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users?email=%s&exact=true", keycloakUrl, realm, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KeycloakUserRecord[]> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakUserRecord[].class);

        KeycloakUserRecord[] users = response.getBody();
        return users != null && users.length > 0;
    }

    public boolean userExistsInKeycloak(UUID userId) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        try {
            ResponseEntity<KeycloakUserRecord> resp =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), KeycloakUserRecord.class);

            return resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null;

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public UUID getUserIdByEmail(String email) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users?email=%s", keycloakUrl, realm, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KeycloakUserRecord[]> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakUserRecord[].class);

        KeycloakUserRecord[] users = response.getBody();
        if (users != null && users.length > 0) {
            return UUID.fromString(users[0].id());
        }
        return null;
    }

    public String getEmailByUserId(UUID userId) {
        KeycloakUserRecord userRecord = getUserById(userId);
        if (userRecord != null) {
            return userRecord.email();
        }
        return null;
    }

    public boolean updateUserDetails(UUID userId,
                                     @Nullable String newEmail,
                                     @Nullable String newFirstName,
                                     @Nullable String newLastName) {


        if ((newEmail == null || newEmail.isBlank())
                && (newFirstName == null || newFirstName.isBlank())
                && (newLastName == null || newLastName.isBlank())) {
            System.out.println("No fields to update for user ID: " + userId);
            System.out.println("At least one field must be provided");
            return false;
        }

        String adminToken = getAdminToken();
            if (adminToken == null) {{
                System.out.println("Failed to obtain admin token from Keycloak");
                return false;
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        String updateUrl = String.format("%s/admin/realms/%s/users/%s", keycloakUrl, realm, userId);

        Map<String, Object> updatePayload = new LinkedHashMap<>();
        if (newEmail != null && !newEmail.isBlank()) {
            updatePayload.put("email", newEmail);
            updatePayload.put("emailVerified", true);
        }
        if (newFirstName != null && !newFirstName.isBlank()) {
            updatePayload.put("firstName", newFirstName);
        }
        if (newLastName != null && !newLastName.isBlank()) {
            updatePayload.put("lastName", newLastName);
        }

        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updatePayload, headers);
        restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, Void.class);

        return true;
    }

    public String createPasswordResetToken(String email) {
        // Create a token from email and current timestamp of type varchar(255)
        return Base64.getUrlEncoder().encodeToString((email + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
    }

    public boolean storePasswordResetToken(UUID userId, String token) {
        try {
            int inserted = dsl.insertInto(PasswordResetToken.PASSWORD_RESET_TOKEN)
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.USER_ID, userId.toString())
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN, token)
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.EXPIRES_AT, LocalDateTime.now().plusMinutes(10))
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.CREATED_AT, LocalDateTime.now())
                    .execute();
            return inserted > 0;
        } catch (DataIntegrityViolationException e) {
            System.out.println("Failed to store password reset token: " + e.getMessage());
            return false;
        }
    }

    public PasswordResetTokenRecord getPasswordResetTokenRecord(String token) {
        return dsl.selectFrom(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .where(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN.eq(token))
                .fetchOne();
    }

    public boolean markPasswordResetTokenAsUsed(String token) {
        int updated = dsl.update(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .set(PasswordResetToken.PASSWORD_RESET_TOKEN.USED, true)
                .where(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN.eq(token))
                .execute();
        return updated > 0;
    }

    public boolean changePassword(String email, String newPassword) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        String url = String.format("%s/admin/realms/%s/users?email=%s", keycloakUrl, realm, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KeycloakUserRecord[]> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakUserRecord[].class);

        KeycloakUserRecord[] users = response.getBody();
        if (users == null || users.length == 0) {
            return false;
        }

        String userId = users[0].id();

        String resetUrl = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakUrl, realm, userId);

        Map<String, Object> credential = new LinkedHashMap<>();
        credential.put("type", "password");
        credential.put("value", newPassword);
        credential.put("temporary", false);

        HttpEntity<Map<String, Object>> resetEntity = new HttpEntity<>(credential, headers);

        restTemplate.exchange(resetUrl, HttpMethod.PUT, resetEntity, Void.class);
        return true;
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

    public boolean setRole(String email, String roleName) {
        UUID userId = getUserIdByEmail(email);
        if (userId == null) {
            return false;
        }
        return setRole(userId, roleName);
    }

    public record KeycloakRoleRepresentation(
            String id,
            String name,
            Boolean composite,
            Boolean clientRole,
            String containerId
    ) {}

    private KeycloakRoleRepresentation ensureRealmRoleExists(String roleName, HttpHeaders headers) {
        KeycloakRoleRepresentation role = getRealmRoleByName(roleName, headers);
        if (role != null) return role;

        String createUrl = String.format("%s/admin/realms/%s/roles", keycloakUrl, realm);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", roleName);

        try {
            restTemplate.exchange(createUrl, HttpMethod.POST, new HttpEntity<>(payload, headers), Void.class);
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            // Role already exists (created in the meantime), ignore
        }

        role = getRealmRoleByName(roleName, headers);
        if (role == null) {
            throw new RuntimeException("Role could not be created/fetched: " + roleName);
        }
        return role;
    }

    private KeycloakRoleRepresentation getRealmRoleByName(String roleName, HttpHeaders headers) {
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s",
                keycloakUrl,
                realm,
                org.springframework.web.util.UriUtils.encodePathSegment(roleName, java.nio.charset.StandardCharsets.UTF_8)
        );

        try {
            ResponseEntity<KeycloakRoleRepresentation> resp =
                    restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), KeycloakRoleRepresentation.class);
            return resp.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private boolean setRole(UUID userId, String roleName) {
        String adminToken = getAdminToken();
        if (adminToken == null) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        KeycloakRoleRepresentation roleRep = ensureRealmRoleExists(roleName, headers);

        String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                keycloakUrl, realm, userId);

        HttpEntity<java.util.List<KeycloakRoleRepresentation>> entity =
                new HttpEntity<>(java.util.Collections.singletonList(roleRep), headers);

        restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, entity, Void.class);
        return true;
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