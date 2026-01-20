package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.UserDslService;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.ApiFormValidator;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserDslService repository;
    private final AuthService authService;
    private final UserGdprPseudonymizationService userGdprPseudonymizationService;
    private final Logger log = LicensingLoggerFactory.getLogger(UserService.class);

    public UserService(UserDslService repository,
                       AuthService authService,
                       UserGdprPseudonymizationService userGdprPseudonymizationService) {
        this.repository = repository;
        this.authService = authService;
        this.userGdprPseudonymizationService = userGdprPseudonymizationService;
    }

    @Transactional
    public String createUserWithInitialPreferences() throws DataIntegrityViolationException {
        String id = UUID.randomUUID().toString();

        int insertedUser = repository.createUser(id);
        int insertedPreferences = repository.createInitialUserNotificationPreferences(id);

        if (insertedPreferences == 0) {
            log.warn("Failed to insert notification preferences for user ID {} into the local database.", id);
        } else {
            log.info("Notification preferences for user ID {} created successfully in the local database.", id);
        }

        if (insertedUser > 0) {
            log.info("User record with ID {} created successfully in the local database.", id);
            return id;
        }

        log.error("Failed to insert user record with ID {} into the local database.", id);
        throw new RuntimeException("Failed to create user record");
    }

    public enum DeleteUserResult {
        OK,
        NOT_FOUND,
        KEYCLOAK_ERROR,
        INTERNAL_ERROR
    }

    @Transactional
    public DeleteUserResult deleteUser(UUID userId) {
        if (!repository.userExists(userId)) {
            log.warn("User ID {} not found in local database for deletion.", userId);
            return DeleteUserResult.NOT_FOUND;
        }

        boolean deletedInKeycloak;
        try {
            deletedInKeycloak = authService.deleteUserInKeycloak(userId);
        } catch (RuntimeException e) {
            log.error("Error occurred while deleting user ID {} in Keycloak: {}", userId, e.getMessage());
            return DeleteUserResult.KEYCLOAK_ERROR;
        }

        if (!deletedInKeycloak) {
            log.error("Failed to delete user ID {} in Keycloak.", userId);
            return DeleteUserResult.NOT_FOUND;
        }

        String result = userGdprPseudonymizationService.pseudonymizeUserIdEverywhere(userId);
        if (result == null) {
            log.warn("User ID {} could not be pseudonymized in all relevant tables.", userId);
            log.warn("Manual cleanup may be required for user ID {} in some tables.", userId);
        }

        if (repository.userExists(userId)) {
            log.error("Failed to delete user ID {} from local database.", userId);
            return DeleteUserResult.INTERNAL_ERROR;
        }

        log.info("User ID {} successfully deleted from both Keycloak and local database.", userId);

        int updatedApplicationCount = repository.setApplicationStatusToCancelled(userId.toString());
        log.info("Updated application status to 'cancelled' for {} applications associated to pseudonymized user.", updatedApplicationCount);

        return DeleteUserResult.OK;
    }

    @Transactional
    public boolean userExists(UUID userId) {
        return repository.userExists(userId);
    }

    @Transactional(readOnly = true)
    public UserResource getUser(UUID userId) {
        if (!repository.userExists(userId)) {
            return null;
        }

        UserResource user = new UserResource();
        AuthService.KeycloakUserRecord kcUser = authService.getUserById(userId);
        if (kcUser != null) {
            user.setUsername(kcUser.username());
            user.setEmail(kcUser.email());
            user.setFirstName(kcUser.firstName());
            user.setLastName(kcUser.lastName());
            user.setEnabled(kcUser.enabled());
            user.setEmailVerified(kcUser.emailVerified());
            user.setCreatedTimestamp(kcUser.createdTimestamp());
        }
        user.setId(userId);
        return user;
    }

    @Transactional
    public List<UserResource> listUsers(int offset, int limit) {
        List<UserRecord> userRecords = repository.listUsers(offset, limit);

        return userRecords.stream().map(record -> {
            UserResource user = new UserResource();
            RecordToResourceMapperUtil.mapUserRecordToResource(record, user);

            UUID userId = UUID.fromString(record.getId());
            AuthService.KeycloakUserRecord kcUser = authService.getUserById(userId);
            if (kcUser != null) {
                user.setUsername(kcUser.username());
                user.setEmail(kcUser.email());
                user.setFirstName(kcUser.firstName());
                user.setLastName(kcUser.lastName());
                user.setEnabled(kcUser.enabled());
                user.setEmailVerified(kcUser.emailVerified());
                user.setCreatedTimestamp(kcUser.createdTimestamp());
            }
            return user;
        }).toList();
    }

    public enum UpdateUserResult {
        OK,
        NOT_FOUND,
        INTERNAL_ERROR
    }

    @Transactional
    public UpdateUserResult updateUser(UUID userId, UpdateUserRequest updateUserRequest) throws RestClientResponseException {
        if (!repository.userExists(userId)) {
            return UpdateUserResult.NOT_FOUND;
        }

        authService.updateUser(userId, updateUserRequest);
        return UpdateUserResult.OK;
    }

    @Transactional(readOnly = true)
    public List<UserNotificationResource> getNotifications(UUID userId) {
        if (!repository.userExists(userId)) {
            return null;
        }
        return repository.getNotifications(userId);
    }

    @Transactional
    public boolean markNotificationRead(UUID notificationId) {
        if (!repository.notificationExists(notificationId)) {
            return false;
        }
        repository.markNotificationRead(notificationId, LocalDateTime.now(Clock.systemUTC()));
        return true;
    }

    @Transactional
    public void createNotification(int applicationId, LocalDateTime now, UUID userId, String message) {
        repository.createNotification(applicationId, now, userId, message);
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResource getNotificationPreferences(UUID userId) {
        NotificationPreferencesRecord record = repository.getNotificationPreferences(userId);
        if (record == null) {
            return null;
        }

        NotificationPreferencesResource apiResource = new NotificationPreferencesResource();
        RecordToResourceMapperUtil.mapNotificationPreferencesRecordToResource(record, apiResource);
        return apiResource;
    }

    @Transactional
    public NotificationPreferencesResource updateNotificationPreferences(UUID userId, NotificationPreferencesUpdate update) {
        NotificationPreferencesRecord updatedRecord = repository.updateNotificationPreferences(userId, update);
        if (updatedRecord == null) {
            return null;
        }

        NotificationPreferencesResource apiResource = new NotificationPreferencesResource();
        RecordToResourceMapperUtil.mapNotificationPreferencesRecordToResource(updatedRecord, apiResource);
        return apiResource;
    }

    public enum ChangePasswordResult {
        OK,
        USER_NOT_FOUND,
        OLD_PASSWORD_WRONG,
        NEW_PASSWORD_INVALID,
        KEYCLOAK_NOT_FOUND
    }

    @Transactional
    public ChangePasswordResult changePassword(UUID userId, ChangePasswordForUserRequest req) {
        String userEmail = authService.getEmailByUserId(userId);
        if (userEmail == null) {
            return ChangePasswordResult.USER_NOT_FOUND;
        }

        boolean oldPasswordCorrect = authService.checkUserPassword(userEmail, req.getOldPassword());
        if (!oldPasswordCorrect) {
            return ChangePasswordResult.OLD_PASSWORD_WRONG;
        }

        ApiFormValidator apiFormValidator = new ApiFormValidator();
        if (!apiFormValidator.isValidPassword(req.getNewPassword())) {
            return ChangePasswordResult.NEW_PASSWORD_INVALID;
        }

        try {
            boolean changed = authService.changePassword(userEmail, req.getNewPassword());
            if (changed) {
                return ChangePasswordResult.OK;
            }
            return ChangePasswordResult.KEYCLOAK_NOT_FOUND;
        } catch (RestClientResponseException e) {
            throw e;
        }
    }
}