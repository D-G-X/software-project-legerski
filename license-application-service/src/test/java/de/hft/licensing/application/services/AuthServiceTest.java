package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.AuthDslService;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import de.hft.licensing.model.ChangeUserdetailsRequest;
import de.hft.licensing.model.LoginRequest;
import de.hft.licensing.model.LoginResource;
import de.hft.licensing.model.RefreshLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthDslService repository;

    @Mock
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, emailService, restTemplate);

        ReflectionTestUtils.setField(service, "realm", "license-realm");
        ReflectionTestUtils.setField(service, "keycloakUrl", "http://keycloak:8080");
        ReflectionTestUtils.setField(service, "clientId", "backend-api");
        ReflectionTestUtils.setField(service, "clientSecret", "secret");
    }

    @Test
    void requestPasswordReset_returnsNOT_FOUND_whenEmailNotRegistered() {
        AuthService spy = spy(service);

        doReturn(false).when(spy).isEmailRegistered("a@b.de");

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.NOT_FOUND, res);
        verify(repository, never()).storePasswordResetToken(any(), any(), any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_returnsNOT_FOUND_whenKeycloakUserIdMissing() {
        AuthService spy = spy(service);

        doReturn(true).when(spy).isEmailRegistered("a@b.de");
        doReturn(null).when(spy).getUserIdByEmail("a@b.de");

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.NOT_FOUND, res);
        verify(repository, never()).storePasswordResetToken(any(), any(), any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_returnsINTERNAL_ERROR_whenTokenCreationFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        doReturn(true).when(spy).isEmailRegistered("a@b.de");
        doReturn(userId).when(spy).getUserIdByEmail("a@b.de");
        doReturn(null).when(spy).createPasswordResetToken("a@b.de");

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.INTERNAL_ERROR, res);
        verify(repository, never()).storePasswordResetToken(any(), any(), any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_returnsINTERNAL_ERROR_whenStoreFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        doReturn(true).when(spy).isEmailRegistered("a@b.de");
        doReturn(userId).when(spy).getUserIdByEmail("a@b.de");
        doReturn("tok").when(spy).createPasswordResetToken("a@b.de");

        when(repository.storePasswordResetToken(eq(userId), eq("tok"), any(), any()))
                .thenReturn(false);

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.INTERNAL_ERROR, res);
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void requestPasswordReset_returnsINTERNAL_ERROR_whenEmailSendFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        doReturn(true).when(spy).isEmailRegistered("a@b.de");
        doReturn(userId).when(spy).getUserIdByEmail("a@b.de");
        doReturn("tok").when(spy).createPasswordResetToken("a@b.de");

        when(repository.storePasswordResetToken(eq(userId), eq("tok"), any(), any()))
                .thenReturn(true);

        when(emailService.sendEmail(eq("a@b.de"), isNull(), anyString(), contains("token=tok")))
                .thenReturn(false);

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.INTERNAL_ERROR, res);
    }

    @Test
    void requestPasswordReset_returnsOK_whenAllStepsSucceed() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        doReturn(true).when(spy).isEmailRegistered("a@b.de");
        doReturn(userId).when(spy).getUserIdByEmail("a@b.de");
        doReturn("tok").when(spy).createPasswordResetToken("a@b.de");

        when(repository.storePasswordResetToken(eq(userId), eq("tok"), any(), any()))
                .thenReturn(true);

        when(emailService.sendEmail(eq("a@b.de"), isNull(), anyString(), contains("token=tok")))
                .thenReturn(true);

        var res = spy.requestPasswordReset("a@b.de");

        assertEquals(AuthService.RequestPasswordResetResult.OK, res);
        verify(repository).storePasswordResetToken(eq(userId), eq("tok"), any(), any());
        verify(emailService).sendEmail(eq("a@b.de"), isNull(), anyString(), contains("token=tok"));
    }

    @Test
    void changeUserPassword_returnsBAD_REQUEST_whenTokenNotFound() {
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(null);

        var res = service.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.BAD_REQUEST, res);
        verify(repository, never()).markPasswordResetTokenAsUsed(anyString());
    }

    @Test
    void changeUserPassword_returnsBAD_REQUEST_whenTokenExpired() {
        PasswordResetTokenRecord rec = tokenRecord(
                "tok",
                UUID.randomUUID().toString(),
                LocalDateTime.now().minusMinutes(1),
                false
        );
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(rec);

        var res = service.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.BAD_REQUEST, res);
        verify(repository, never()).markPasswordResetTokenAsUsed(anyString());
    }

    @Test
    void changeUserPassword_returnsBAD_REQUEST_whenTokenAlreadyUsed() {
        PasswordResetTokenRecord rec = tokenRecord(
                "tok",
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(10),
                true
        );
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(rec);

        var res = service.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.BAD_REQUEST, res);
        verify(repository, never()).markPasswordResetTokenAsUsed(anyString());
    }

    @Test
    void changeUserPassword_returnsINTERNAL_ERROR_whenPasswordChangeFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        PasswordResetTokenRecord rec = tokenRecord(
                "tok",
                userId.toString(),
                LocalDateTime.now().plusMinutes(10),
                false
        );
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(rec);

        doReturn("a@b.de").when(spy).getEmailByUserId(userId);
        doReturn(false).when(spy).changePassword("a@b.de", "newPass");

        var res = spy.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.INTERNAL_ERROR, res);
        verify(repository, never()).markPasswordResetTokenAsUsed(anyString());
    }

    @Test
    void changeUserPassword_returnsINTERNAL_ERROR_whenMarkUsedFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        PasswordResetTokenRecord rec = tokenRecord(
                "tok",
                userId.toString(),
                LocalDateTime.now().plusMinutes(10),
                false
        );
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(rec);

        doReturn("a@b.de").when(spy).getEmailByUserId(userId);
        doReturn(true).when(spy).changePassword("a@b.de", "newPass");

        when(repository.markPasswordResetTokenAsUsed("tok")).thenReturn(false);

        var res = spy.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.INTERNAL_ERROR, res);
    }

    @Test
    void changeUserPassword_returnsOK_whenAllStepsSucceed() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();
        PasswordResetTokenRecord rec = tokenRecord(
                "tok",
                userId.toString(),
                LocalDateTime.now().plusMinutes(10),
                false
        );
        when(repository.getPasswordResetTokenRecord("tok")).thenReturn(rec);

        doReturn("a@b.de").when(spy).getEmailByUserId(userId);
        doReturn(true).when(spy).changePassword("a@b.de", "newPass");
        when(repository.markPasswordResetTokenAsUsed("tok")).thenReturn(true);

        var res = spy.changeUserPassword("tok", "newPass");

        assertEquals(AuthService.ChangePasswordResult.OK, res);
        verify(repository).markPasswordResetTokenAsUsed("tok");
    }

    @Test
    void changeUserDetails_returnsBAD_REQUEST_whenEmailInvalid() {
        AuthService spy = spy(service);

        ChangeUserdetailsRequest req = new ChangeUserdetailsRequest();
        req.setEmail("not-an-email");
        req.setFirstname("Max");
        req.setLastname("Mustermann");

        var res = spy.changeUserDetails(UUID.randomUUID(), req);

        assertEquals(AuthService.ChangeUserDetailsResult.BAD_REQUEST, res);
        verify(spy, never()).updateUserDetails(any(), any(), any(), any());
    }

    @Test
    void changeUserDetails_returnsCONFLICT_whenEmailAlreadyRegistered() {
        AuthService spy = spy(service);

        ChangeUserdetailsRequest req = new ChangeUserdetailsRequest();
        req.setEmail("max@uni.de");
        req.setFirstname("Max");
        req.setLastname("Mustermann");

        doReturn(true).when(spy).isEmailRegistered("max@uni.de");

        var res = spy.changeUserDetails(UUID.randomUUID(), req);

        assertEquals(AuthService.ChangeUserDetailsResult.CONFLICT, res);
        verify(spy, never()).updateUserDetails(any(), any(), any(), any());
    }

    @Test
    void changeUserDetails_returnsOK_whenUpdateSucceeds() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();

        ChangeUserdetailsRequest req = new ChangeUserdetailsRequest();
        req.setEmail("max@uni.de");
        req.setFirstname("Max");
        req.setLastname("Mustermann");

        doReturn(false).when(spy).isEmailRegistered("max@uni.de");
        doReturn(true).when(spy).updateUserDetails(eq(userId), eq("max@uni.de"), eq("Max"), eq("Mustermann"));

        var res = spy.changeUserDetails(userId, req);

        assertEquals(AuthService.ChangeUserDetailsResult.OK, res);
        verify(spy).updateUserDetails(eq(userId), eq("max@uni.de"), eq("Max"), eq("Mustermann"));
    }

    @Test
    void changeUserDetails_returnsFAILED_whenUpdateFails() {
        AuthService spy = spy(service);

        UUID userId = UUID.randomUUID();

        ChangeUserdetailsRequest req = new ChangeUserdetailsRequest();
        req.setEmail("max@uni.de");
        req.setFirstname("Max");
        req.setLastname("Mustermann");

        doReturn(false).when(spy).isEmailRegistered("max@uni.de");
        doReturn(false).when(spy).updateUserDetails(eq(userId), any(), any(), any());

        var res = spy.changeUserDetails(userId, req);

        assertEquals(AuthService.ChangeUserDetailsResult.FAILED, res);
    }

    @Test
    void login_setsIsAdminTrue_whenTokenHasAdminRole() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.de");
        req.setPassword("pw");

        LoginResource lr = new LoginResource();
        lr.setAccessToken(jwtWithRoles("admin"));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(LoginResource.class)))
                .thenReturn(ResponseEntity.ok(lr));

        LoginResource out = service.login(req);

        assertNotNull(out);
        assertEquals(Boolean.TRUE, out.getIsAdmin());
    }

    @Test
    void login_setsIsAdminFalse_whenTokenHasNoAdminRole() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.de");
        req.setPassword("pw");

        LoginResource lr = new LoginResource();
        lr.setAccessToken(jwtWithRoles("user"));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(LoginResource.class)))
                .thenReturn(ResponseEntity.ok(lr));

        LoginResource out = service.login(req);

        assertNotNull(out);
        assertNotEquals(Boolean.TRUE, out.getIsAdmin());
    }

    @Test
    void login_setsIsAdminFalse_whenTokenInvalid() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.de");
        req.setPassword("pw");

        LoginResource lr = new LoginResource();
        lr.setAccessToken("not-a-jwt");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(LoginResource.class)))
                .thenReturn(ResponseEntity.ok(lr));

        LoginResource out = service.login(req);

        assertNotNull(out);
        assertNotEquals(Boolean.TRUE, out.getIsAdmin());
    }

    @Test
    void refreshLogin_setsIsAdminTrue_whenTokenHasAdminRole() {
        RefreshLoginRequest req = new RefreshLoginRequest();
        req.setRefreshToken("r1");

        LoginResource lr = new LoginResource();
        lr.setAccessToken(jwtWithRoles("admin"));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(LoginResource.class)))
                .thenReturn(ResponseEntity.ok(lr));

        LoginResource out = service.refreshLogin(req);

        assertNotNull(out);
        assertEquals(Boolean.TRUE, out.getIsAdmin());
    }

    @Test
    void checkUserPassword_returnsTrue_whenLoginReturnsAccessToken() {
        AuthService spy = spy(service);

        LoginResource lr = new LoginResource();
        lr.setAccessToken("x");

        doReturn(lr).when(spy).login(any());

        assertTrue(spy.checkUserPassword("a@b.de", "pw"));
    }

    @Test
    void checkUserPassword_returnsFalse_on401() {
        AuthService spy = spy(service);

        doThrow(HttpClientErrorException.Unauthorized.create(
                HttpStatus.UNAUTHORIZED, "unauth", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8
        )).when(spy).login(any());

        assertFalse(spy.checkUserPassword("a@b.de", "pw"));
    }

    @Test
    void checkUserPassword_throwsRuntime_onOtherErrors() {
        AuthService spy = spy(service);

        doThrow(HttpClientErrorException.Forbidden.create(
                HttpStatus.FORBIDDEN, "forbidden", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8
        )).when(spy).login(any());

        assertThrows(RuntimeException.class, () -> spy.checkUserPassword("a@b.de", "pw"));
    }

    @Test
    void isEmailRegistered_true_whenKeycloakReturnsUsers() {
        stubAdminToken("adm");

        AuthService.KeycloakUserRecord[] users = new AuthService.KeycloakUserRecord[]{
                new AuthService.KeycloakUserRecord("11111111-1111-1111-1111-111111111111", "u", "e", "f", "l", true, true, 1L)
        };

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(users));

        assertTrue(service.isEmailRegistered("x@y.de"));
    }

    @Test
    void isEmailRegistered_false_whenEmpty() {
        stubAdminToken("adm");

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(new AuthService.KeycloakUserRecord[0]));

        assertFalse(service.isEmailRegistered("x@y.de"));
    }

    @Test
    void getUserIdByEmail_returnsUuid_whenFound() {
        stubAdminToken("adm");

        var id = "22222222-2222-2222-2222-222222222222";
        AuthService.KeycloakUserRecord[] users = new AuthService.KeycloakUserRecord[]{
                new AuthService.KeycloakUserRecord(id, "u", "e", "f", "l", true, true, 1L)
        };

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(users));

        assertEquals(UUID.fromString(id), service.getUserIdByEmail("x@y.de"));
    }

    @Test
    void getUserIdByEmail_returnsNull_whenNotFound() {
        stubAdminToken("adm");

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(new AuthService.KeycloakUserRecord[0]));

        assertNull(service.getUserIdByEmail("x@y.de"));
    }

    @Test
    void updateUserDetails_returnsFalse_whenNoFieldsProvided() {
        assertFalse(service.updateUserDetails(UUID.randomUUID(), "  ", null, ""));
    }

    @Test
    void updateUserDetails_returnsFalse_whenAdminTokenNull() {
        // getAdminToken -> null: response body ohne access_token
        when(restTemplate.exchange(
                contains("/realms/master/protocol/openid-connect/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of()));

        assertFalse(service.updateUserDetails(UUID.randomUUID(), "a@b.de", "Max", "Mu"));
    }

    @Test
    void updateUserDetails_returnsTrue_whenPutSucceeds() {
        stubAdminToken("adm");

        when(restTemplate.exchange(contains("/admin/realms/"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertTrue(service.updateUserDetails(UUID.randomUUID(), "a@b.de", "Max", "Mu"));
    }

    @Test
    void changePassword_returnsFalse_whenUserNotFound() {
        stubAdminToken("adm");

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(new AuthService.KeycloakUserRecord[0]));

        assertFalse(service.changePassword("x@y.de", "new"));
    }

    @Test
    void changePassword_returnsTrue_whenResetPutSucceeds() {
        stubAdminToken("adm");

        var id = "33333333-3333-3333-3333-333333333333";
        AuthService.KeycloakUserRecord[] users = new AuthService.KeycloakUserRecord[]{
                new AuthService.KeycloakUserRecord(id, "u", "e", "f", "l", true, true, 1L)
        };

        when(restTemplate.exchange(contains("/users?email="), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord[].class)))
                .thenReturn(ResponseEntity.ok(users));

        when(restTemplate.exchange(contains("/reset-password"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertTrue(service.changePassword("x@y.de", "new"));
    }

    @Test
    void getUserById_returnsNull_whenAdminTokenNull() {
        when(restTemplate.exchange(
                contains("/realms/master/protocol/openid-connect/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of())); // access_token fehlt

        assertNull(service.getUserById(UUID.randomUUID()));
    }

    @Test
    void getUserById_returnsRecord_when2xxAndBodyPresent() {
        stubAdminToken("adm");

        var rec = new AuthService.KeycloakUserRecord(
                "44444444-4444-4444-4444-444444444444", "u", "mail@x.de", "f", "l", true, true, 1L
        );

        when(restTemplate.exchange(contains("/admin/realms/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord.class)))
                .thenReturn(ResponseEntity.ok(rec));

        assertNotNull(service.getUserById(UUID.randomUUID()));
    }

    @Test
    void getEmailByUserId_returnsNull_whenUserNull() {
        AuthService spy = spy(service);
        doReturn(null).when(spy).getUserById(any());
        assertNull(spy.getEmailByUserId(UUID.randomUUID()));
    }

    @Test
    void userExistsInKeycloak_returnsTrue_whenKeycloakReturnsUser() {
        stubAdminToken("adm");

        var rec = new AuthService.KeycloakUserRecord(
                "55555555-5555-5555-5555-555555555555", "u", "mail@x.de", "f", "l", true, true, 1L
        );

        when(restTemplate.exchange(contains("/admin/realms/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord.class)))
                .thenReturn(ResponseEntity.ok(rec));

        assertTrue(service.userExistsInKeycloak(UUID.randomUUID()));
    }

    @Test
    void userExistsInKeycloak_returnsFalse_on404() {
        stubAdminToken("adm");

        when(restTemplate.exchange(contains("/admin/realms/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthService.KeycloakUserRecord.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "not found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8
                ));

        assertFalse(service.userExistsInKeycloak(UUID.randomUUID()));
    }

    @Test
    void createPasswordResetToken_returnsNonNull() {
        String t = service.createPasswordResetToken("a@b.de");
        assertNotNull(t);
        assertFalse(t.isBlank());
    }

    private void stubAdminToken(String token) {
        when(restTemplate.exchange(
                contains("/realms/master/protocol/openid-connect/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("access_token", token)));
    }

    private static String jwtWithRoles(String... roles) {
        String payloadJson = """
        {"realm_access":{"roles":%s}}
        """.formatted(java.util.Arrays.stream(roles).map(r -> "\"" + r + "\"").toList());
        String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "header." + payloadB64 + ".sig";
    }

    private static PasswordResetTokenRecord tokenRecord(String token, String userId, LocalDateTime expiresAt, boolean used) {
        PasswordResetTokenRecord r = new PasswordResetTokenRecord();
        r.setToken(token);
        r.setUserId(userId);
        r.setExpiresAt(expiresAt);
        r.setUsed(used);
        return r;
    }
}