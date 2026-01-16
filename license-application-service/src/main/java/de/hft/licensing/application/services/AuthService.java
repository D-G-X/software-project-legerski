package de.hft.licensing.application.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.application.repository.AuthDslService;
import de.hft.licensing.utils.ApiFormValidator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthDslService repository;
    private final Logger log = LicensingLoggerFactory.getLogger(AuthService.class);

    public AuthService(AuthDslService repository) {
        this.repository = repository;
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
    private final ApiFormValidator formValidator = new ApiFormValidator();

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

        if (loginResource != null && loginResource.getAccessToken() != null) {
            boolean isAdmin = tokenHasAdminRole(loginResource.getAccessToken());
            loginResource.setIsAdmin(isAdmin);
        }
        log.info("User logged in: {}, isAdmin: {}", request.getEmail(), loginResource != null && Boolean.TRUE.equals(loginResource.getIsAdmin()));
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

    @Transactional
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
            if (id != null) {
                repository.ensureLocalUserAndDefaults(id);
            }

            RegisterResource rr = new RegisterResource();
            rr.setUserId(id);
            rr.setMessage("User registered successfully");
            return rr;

        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
            UUID existingId = getUserIdByEmail(request.getEmail());
            if (existingId == null) throw new RuntimeException("User exists in Keycloak but could not be fetched by email", e);

            repository.ensureLocalUserAndDefaults(existingId);

            RegisterResource rr = new RegisterResource();
            rr.setUserId(existingId);
            rr.setMessage("User already exists");
            return rr;
        }
    }

    public enum RequestPasswordResetResult {
        OK,
        NOT_FOUND,
        INTERNAL_ERROR
    }

    @Transactional
    public RequestPasswordResetResult requestPasswordReset(String email) {
        boolean isEmailRegistered = isEmailRegistered(email);
        if (!isEmailRegistered) {
            System.out.println("Password reset requested for unregistered email: " + email);
            return RequestPasswordResetResult.NOT_FOUND;
        }

        UUID userId = getUserIdByEmail(email);
        if (userId == null) {
            System.out.println("No user found in Keycloak with email " + email);
            return RequestPasswordResetResult.NOT_FOUND;
        }

        String token = createPasswordResetToken(email);
        if (token == null) {
            System.out.println("Failed to create password reset token for email: " + email);
            return RequestPasswordResetResult.INTERNAL_ERROR;
        }

        boolean stored = repository.storePasswordResetToken(userId, token, LocalDateTime.now(Clock.systemUTC()).plusMinutes(10), LocalDateTime.now(Clock.systemUTC()));
        if (!stored) {
            System.out.println("Failed to store password reset token for email: " + email);
            return RequestPasswordResetResult.INTERNAL_ERROR;
        }

        boolean isEmailSent = EmailService.sendEmail(
                email,
                null,
                "Password Reset Request",
                "Click the link to reset your password: <a href=\"http://localhost:3000/reset-password?token=" + token + "\">Reset Password</a>"
        );

        if (!isEmailSent) {
            System.out.println("Failed to send password reset email to: " + email);
            return RequestPasswordResetResult.INTERNAL_ERROR;
        }

        return RequestPasswordResetResult.OK;
    }

    public enum ChangeUserDetailsResult {
        OK,
        BAD_REQUEST,
        CONFLICT,
        FAILED
    }

    public ChangeUserDetailsResult changeUserDetails(UUID userId, ChangeUserdetailsRequest req) {
        String newEmail;
        String newFirstname;
        String newLastname;

        if (req.getEmail() != null && formValidator.isValidEmail(req.getEmail())) {
            newEmail = req.getEmail();
            if (isEmailRegistered(newEmail)) {
                System.out.println("Email already registered: " + req.getEmail());
                return ChangeUserDetailsResult.CONFLICT;
            }
        } else {
            System.out.println("Invalid email format: " + req.getEmail());
            return ChangeUserDetailsResult.BAD_REQUEST;
        }

        if (req.getFirstname() != null && formValidator.isValidName(req.getFirstname())) {
            newFirstname = req.getFirstname();
        } else {
            System.out.println("Invalid firstname format: " + req.getFirstname());
            return ChangeUserDetailsResult.BAD_REQUEST;
        }

        if (req.getLastname() != null && formValidator.isValidName(req.getLastname())) {
            newLastname = req.getLastname();
        } else {
            System.out.println("Invalid lastname format: " + req.getLastname());
            return ChangeUserDetailsResult.BAD_REQUEST;
        }

        boolean updated = updateUserDetails(userId, newEmail, newFirstname, newLastname);
        if (!updated) {
            System.out.println("Failed to update user details for user ID: " + userId);
            return ChangeUserDetailsResult.FAILED;
        }

        return ChangeUserDetailsResult.OK;
    }

    public enum ChangePasswordResult {
        OK,
        BAD_REQUEST,
        INTERNAL_ERROR
    }

    @Transactional
    public ChangePasswordResult changeUserPassword(String token, String newPassword) {
        PasswordResetTokenRecord tokenRecord = repository.getPasswordResetTokenRecord(token);

        if (tokenRecord == null) {
            System.out.println("Invalid password reset token: " + token);
            return ChangePasswordResult.BAD_REQUEST;
        }

        Date now = new Date();
        Date expiresAt = java.sql.Timestamp.valueOf(tokenRecord.getExpiresAt());
        if (now.after(expiresAt)) {
            System.out.println("Expired password reset token for user ID: " + tokenRecord.getUserId());
            return ChangePasswordResult.BAD_REQUEST;
        }

        if (tokenRecord.getUsed()) {
            System.out.println("Already used password reset token for user ID: " + tokenRecord.getUserId());
            return ChangePasswordResult.BAD_REQUEST;
        }

        String userEmail = getEmailByUserId(UUID.fromString(tokenRecord.getUserId()));
        boolean isPasswordChanged = changePassword(userEmail, newPassword);
        if (!isPasswordChanged) {
            System.out.println("Failed to change password for email: " + userEmail);
            return ChangePasswordResult.INTERNAL_ERROR;
        }

        boolean isTokenMarkedUsed = repository.markPasswordResetTokenAsUsed(token);
        if (!isTokenMarkedUsed) {
            System.out.println("Failed to mark password reset token as used for token: " + token);
            return ChangePasswordResult.INTERNAL_ERROR;
        }

        return ChangePasswordResult.OK;
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
        if (adminToken == null) {
            System.out.println("Failed to obtain admin token from Keycloak");
            return false;
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
        return Base64.getUrlEncoder().encodeToString((email + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
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

    private boolean tokenHasAdminRole(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) return false;
            String payloadB64 = parts[1];
            int padding = (4 - (payloadB64.length() % 4)) % 4;
            payloadB64 += "=".repeat(padding);
            byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
            JsonNode payload = objectMapper.readTree(decoded);

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