package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.UserDslService;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDslService repository;

    @Mock
    private AuthService authService;

    @Mock
    private UserGdprPseudonymizationService userGdprPseudonymizationService;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, authService, userGdprPseudonymizationService);
    }

    @Test
    void createUserWithInitialPreferences_returnsId_whenUserInserted_andPreferencesInserted() {
        when(repository.createUser(anyString())).thenReturn(1);
        when(repository.createInitialUserNotificationPreferences(anyString())).thenReturn(1);

        String id = service.createUserWithInitialPreferences();

        assertNotNull(id);
        verify(repository).createUser(eq(id));
        verify(repository).createInitialUserNotificationPreferences(eq(id));
    }

    @Test
    void createUserWithInitialPreferences_returnsId_whenUserInserted_andPreferencesNotInserted() {
        when(repository.createUser(anyString())).thenReturn(1);
        when(repository.createInitialUserNotificationPreferences(anyString())).thenReturn(0);

        String id = service.createUserWithInitialPreferences();

        assertNotNull(id);
        verify(repository).createUser(eq(id));
        verify(repository).createInitialUserNotificationPreferences(eq(id));
    }

    @Test
    void createUserWithInitialPreferences_throwsRuntimeException_whenUserNotInserted() {
        when(repository.createUser(anyString())).thenReturn(0);
        when(repository.createInitialUserNotificationPreferences(anyString())).thenReturn(1);

        assertThrows(RuntimeException.class, () -> service.createUserWithInitialPreferences());
    }

    @Test
    void deleteUser_returnsNOT_FOUND_whenUserNotInLocalDb() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(false);

        assertEquals(UserService.DeleteUserResult.NOT_FOUND, service.deleteUser(userId));
        verifyNoInteractions(authService);
        verifyNoInteractions(userGdprPseudonymizationService);
    }

    @Test
    void deleteUser_returnsKEYCLOAK_ERROR_whenKeycloakThrows() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);
        when(authService.deleteUserInKeycloak(userId)).thenThrow(new RuntimeException("kc down"));

        assertEquals(UserService.DeleteUserResult.KEYCLOAK_ERROR, service.deleteUser(userId));
    }

    @Test
    void deleteUser_returnsNOT_FOUND_whenKeycloakSaysNotDeleted() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);
        when(authService.deleteUserInKeycloak(userId)).thenReturn(false);

        assertEquals(UserService.DeleteUserResult.NOT_FOUND, service.deleteUser(userId));
    }

    @Test
    void deleteUser_returnsINTERNAL_ERROR_whenUserStillExistsAfterPseudonymization() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true).thenReturn(true);
        when(authService.deleteUserInKeycloak(userId)).thenReturn(true);
        when(userGdprPseudonymizationService.pseudonymizeUserIdEverywhere(userId)).thenReturn("newId");

        assertEquals(UserService.DeleteUserResult.INTERNAL_ERROR, service.deleteUser(userId));
    }

    @Test
    void deleteUser_returnsOK_whenAllGood_andPseudonymizationNullAllowed() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true).thenReturn(false);
        when(authService.deleteUserInKeycloak(userId)).thenReturn(true);
        when(userGdprPseudonymizationService.pseudonymizeUserIdEverywhere(userId)).thenReturn(null);
        when(repository.setApplicationStatusToCancelled(userId.toString())).thenReturn(3);

        assertEquals(UserService.DeleteUserResult.OK, service.deleteUser(userId));
        verify(repository).setApplicationStatusToCancelled(userId.toString());
    }

    @Test
    void getUser_returnsNull_whenUserNotExists() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(false);

        assertNull(service.getUser(userId));
        verifyNoInteractions(authService);
    }

    @Test
    void getUser_returnsUserResource_withKeycloakFields_whenKeycloakUserPresent() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);

        AuthService.KeycloakUserRecord kc = new AuthService.KeycloakUserRecord(
                userId.toString(), "uname", "mail@x.de", "First", "Last", true, true, 123L
        );
        when(authService.getUserById(userId)).thenReturn(kc);

        UserResource res = service.getUser(userId);

        assertNotNull(res);
        assertEquals(userId, res.getId());
        assertEquals("uname", res.getUsername());
        assertEquals("mail@x.de", res.getEmail());
    }

    @Test
    void getUser_returnsUserResource_withOnlyId_whenKeycloakUserNull() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);
        when(authService.getUserById(userId)).thenReturn(null);

        UserResource res = service.getUser(userId);

        assertNotNull(res);
        assertEquals(userId, res.getId());
        assertNull(res.getUsername());
        assertNull(res.getEmail());
    }

    @Test
    void listUsers_mapsEachUser_andEnrichesWhenKeycloakPresent_orNull() {
        UserRecord r1 = new UserRecord();
        UUID id1 = UUID.randomUUID();
        r1.setId(id1.toString());

        UserRecord r2 = new UserRecord();
        UUID id2 = UUID.randomUUID();
        r2.setId(id2.toString());

        when(repository.listUsers(0, 2)).thenReturn(List.of(r1, r2));

        AuthService.KeycloakUserRecord kc1 = new AuthService.KeycloakUserRecord(
                id1.toString(), "u1", "e1@x.de", "F1", "L1", true, true, 1L
        );

        when(authService.getUserById(id1)).thenReturn(kc1);
        when(authService.getUserById(id2)).thenReturn(null);

        List<UserResource> res = service.listUsers(0, 2);

        assertEquals(2, res.size());
        assertEquals(id1, res.get(0).getId());
        assertEquals("u1", res.get(0).getUsername());
        assertEquals(id2, res.get(1).getId());
    }

    @Test
    void updateUser_returnsNOT_FOUND_whenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(false);

        assertEquals(UserService.UpdateUserResult.NOT_FOUND, service.updateUser(userId, new UpdateUserRequest()));
        verifyNoInteractions(authService);
    }

    @Test
    void updateUser_returnsOK_whenUserExists_andDelegatesToAuth() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);

        UpdateUserRequest req = new UpdateUserRequest();
        assertEquals(UserService.UpdateUserResult.OK, service.updateUser(userId, req));
        verify(authService).updateUser(userId, req);
    }

    @Test
    void getNotifications_returnsNull_whenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(false);

        assertNull(service.getNotifications(userId));
    }

    @Test
    void getNotifications_returnsList_whenUserExists() {
        UUID userId = UUID.randomUUID();
        when(repository.userExists(userId)).thenReturn(true);

        List<UserNotificationResource> list = List.of(new UserNotificationResource());
        when(repository.getNotifications(userId)).thenReturn(list);

        assertSame(list, service.getNotifications(userId));
    }

    @Test
    void markNotificationRead_returnsFalse_whenNotificationMissing() {
        UUID nid = UUID.randomUUID();
        when(repository.notificationExists(nid)).thenReturn(false);

        assertFalse(service.markNotificationRead(nid));
        verify(repository, never()).markNotificationRead(any(), any(LocalDateTime.class));
    }

    @Test
    void markNotificationRead_returnsTrue_whenNotificationExists_andMarks() {
        UUID nid = UUID.randomUUID();
        when(repository.notificationExists(nid)).thenReturn(true);

        assertTrue(service.markNotificationRead(nid));
        verify(repository).markNotificationRead(eq(nid), any(LocalDateTime.class));
    }

    @Test
    void getNotificationPreferences_returnsNull_whenRecordNull() {
        UUID userId = UUID.randomUUID();
        when(repository.getNotificationPreferences(userId)).thenReturn(null);

        assertNull(service.getNotificationPreferences(userId));
    }

    @Test
    void getNotificationPreferences_returnsMappedResource_whenRecordPresent() {
        UUID userId = UUID.randomUUID();
        NotificationPreferencesRecord rec = new NotificationPreferencesRecord();
        rec.setUserId(String.valueOf(userId));
        when(repository.getNotificationPreferences(userId)).thenReturn(rec);

        assertNotNull(service.getNotificationPreferences(userId));
    }

    @Test
    void updateNotificationPreferences_returnsNull_whenUpdateReturnsNull() {
        UUID userId = UUID.randomUUID();
        NotificationPreferencesUpdate upd = new NotificationPreferencesUpdate();

        when(repository.updateNotificationPreferences(userId, upd)).thenReturn(null);

        assertNull(service.updateNotificationPreferences(userId, upd));
    }

    @Test
    void updateNotificationPreferences_returnsMappedResource_whenUpdateReturnsRecord() {
        UUID userId = UUID.randomUUID();

        NotificationPreferencesUpdate upd = new NotificationPreferencesUpdate();
        upd.setNotificationWay(NotificationWayApiEnum.EMAIL);
        upd.setApplicationUpdatesNotification(true);
        upd.setLicenseRenewalNotification(false);

        NotificationPreferencesRecord rec = new NotificationPreferencesRecord();
        rec.setUserId(String.valueOf(userId));

        when(repository.updateNotificationPreferences(userId, upd)).thenReturn(rec);

        assertNotNull(service.updateNotificationPreferences(userId, upd));
    }

    @Test
    void changePassword_returnsUSER_NOT_FOUND_whenEmailNull() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPassword123!");

        when(authService.getEmailByUserId(userId)).thenReturn(null);

        assertEquals(UserService.ChangePasswordResult.USER_NOT_FOUND, service.changePassword(userId, req));
    }

    @Test
    void changePassword_returnsOLD_PASSWORD_WRONG_whenOldPasswordIncorrect() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPassword123!");

        when(authService.getEmailByUserId(userId)).thenReturn("a@b.de");
        when(authService.checkUserPassword("a@b.de", "old")).thenReturn(false);

        assertEquals(UserService.ChangePasswordResult.OLD_PASSWORD_WRONG, service.changePassword(userId, req));
    }

    @Test
    void changePassword_returnsNEW_PASSWORD_INVALID_whenValidatorFails() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("short");

        when(authService.getEmailByUserId(userId)).thenReturn("a@b.de");
        when(authService.checkUserPassword("a@b.de", "old")).thenReturn(true);

        assertEquals(UserService.ChangePasswordResult.NEW_PASSWORD_INVALID, service.changePassword(userId, req));
    }

    @Test
    void changePassword_returnsOK_whenChangedTrue() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPassword123!");

        when(authService.getEmailByUserId(userId)).thenReturn("a@b.de");
        when(authService.checkUserPassword("a@b.de", "old")).thenReturn(true);
        when(authService.changePassword("a@b.de", "NewPassword123!")).thenReturn(true);

        assertEquals(UserService.ChangePasswordResult.OK, service.changePassword(userId, req));
    }

    @Test
    void changePassword_returnsKEYCLOAK_NOT_FOUND_whenChangedFalse() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPassword123!");

        when(authService.getEmailByUserId(userId)).thenReturn("a@b.de");
        when(authService.checkUserPassword("a@b.de", "old")).thenReturn(true);
        when(authService.changePassword("a@b.de", "NewPassword123!")).thenReturn(false);

        assertEquals(UserService.ChangePasswordResult.KEYCLOAK_NOT_FOUND, service.changePassword(userId, req));
    }

    @Test
    void changePassword_rethrowsRestClientResponseException() {
        UUID userId = UUID.randomUUID();
        ChangePasswordForUserRequest req = new ChangePasswordForUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("NewPassword123!");

        when(authService.getEmailByUserId(userId)).thenReturn("a@b.de");
        when(authService.checkUserPassword("a@b.de", "old")).thenReturn(true);

        RestClientResponseException ex = HttpClientErrorException.Unauthorized.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "unauth",
                org.springframework.http.HttpHeaders.EMPTY, new byte[0], java.nio.charset.StandardCharsets.UTF_8
        );
        when(authService.changePassword("a@b.de", "NewPassword123!")).thenThrow(ex);

        assertThrows(RestClientResponseException.class, () -> service.changePassword(userId, req));
    }
}