package de.hft.licensing.rest;

import de.hft.licensing.api.UsersApi;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.Notification;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.services.KeycloakAuthService;
import de.hft.licensing.services.UserGdprPseudonymizationService;
import de.hft.licensing.services.auth.AdminOnly;
import de.hft.licensing.services.dslService.UserDslService;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class UsersController implements UsersApi {

    private final DSLContext dsl;
    private final UserDslService userDslService;
    private final KeycloakAuthService keycloakAuthService;
    private final UserGdprPseudonymizationService userGdprPseudonymizationService;
    private final Logger log = LicensingLoggerFactory.getLogger(UsersController.class);

    public UsersController(DSLContext dsl, KeycloakAuthService keycloakAuthService, UserGdprPseudonymizationService userGdprPseudonymizationService, LoggersEndpoint loggersEndpoint, UserDslService userDslService) {
        this.keycloakAuthService = keycloakAuthService;
        this.dsl = dsl;
        this.userGdprPseudonymizationService = userGdprPseudonymizationService;
        this.userDslService = userDslService;
    }

    // ONLY FOR ADMINISTRATION PURPOSES - DO NOT USE IN PRODUCTION
    @Override
    @AdminOnly
    @Transactional
    public ResponseEntity<Void> createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null || createUserRequest.getSchema() == null || createUserRequest.getSchema().getUsername() == null) {
            log.error("Invalid create user request received.");
            return ResponseEntity.badRequest().build();
        }

        String id = UUID.randomUUID().toString();

        try {
            // Create User record
            int insertedUser = dsl.insertInto(User.USER)
                    .set(User.USER.ID, id)
                    .execute();

            // Create user's Notification Preferences record with default values
            int insertedPreferences = dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID, id)
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY, (NotificationWay) EnumMapperUtil.getPendantFromEnum(NotificationWayApiEnum.NONE))
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION, true)
                    .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION, true)
                    .execute();
            if (insertedPreferences == 0) {
                log.warn("Failed to insert notification preferences for user ID {} into the local database.", id);
            } else {
                log.info("Notification preferences for user ID {} created successfully in the local database.", id);
            }

            if (insertedUser > 0) {
                log.info("User record with ID {} created successfully in the local database.", id);
                return ResponseEntity.created(URI.create("/users/" + id)).build();
            } else {
                log.error("Failed to insert user record with ID {} into the local database.", id);
                return ResponseEntity.status(500).build();
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating user record with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(409).build();
        }
    }

    @Override
    @PreAuthorize("@userAuthorization.canAccessUser(authentication, #userId)")
    @Transactional
    public ResponseEntity<Void> deleteUser(UUID userId) {
        if (userId == null) {
            log.error("Invalid user ID provided for deletion.");
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId.toString()))
        );
        if (!exists) {
            log.warn("User ID {} not found in local database for deletion.", userId);
            return ResponseEntity.notFound().build();
        }

        boolean deletedInKeycloak;
        try {
            deletedInKeycloak = keycloakAuthService.deleteUserInKeycloak(userId);
        } catch (RuntimeException e) {
            log.error("Error occurred while deleting user ID {} in Keycloak: {}", userId, e.getMessage());
            return ResponseEntity.status(502).build();
        }

        if (!deletedInKeycloak) {
            log.error("Failed to delete user ID {} in Keycloak.", userId);
            return ResponseEntity.notFound().build();
        }

        String result = userGdprPseudonymizationService.pseudonymizeUserIdEverywhere(userId);
        if (result == null){
            log.warn("User ID {} could not be pseudonymized in all relevant tables.", userId);
            log.warn("Manual cleanup may be required for user ID {} in some tables.", userId);
        }

        boolean deleted = !dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId.toString()))
        );

        if (!deleted) {
            log.error("Failed to delete user ID {} from local database.", userId);
            return ResponseEntity.status(500).build();
        }

        log.info("User ID {} successfully deleted from both Keycloak and local database.", userId);

        int updatedApplicationCount = userDslService.setApplicationStatusToCancelled(userId.toString());
        log.info("Updated application status to 'cancelled' for {} applications associated to pseudonymized user.", updatedApplicationCount);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("@userAuthorization.canAccessUser(authentication, #userId)")
    public ResponseEntity<UserResource> getUser(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(User.USER).where(User.USER.ID.eq(userId.toString()))
        );

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        UserResource user = new UserResource();
        KeycloakAuthService.KeycloakUserRecord kcUser = keycloakAuthService.getUserById(userId);
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
        return ResponseEntity.ok(user);
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<UserResource>> listUsers(String username, String email, Integer first, Integer max) {
        // only column is id, so ignore filters for now
        int offset = (first == null || first < 0) ? 0 : first;
        int limit = (max == null || max <= 0) ? 100 : Math.min(max, 100);

        var userRecords = dsl.select()
                .from(User.USER)
                .offset(offset)
                .limit(limit)
                .fetchInto(UserRecord.class);

        List<UserResource> result = userRecords.stream().map(record -> {
            UserResource user = new UserResource();
            RecordToResourceMapperUtil.mapUserRecordToResource(record, user);

            UUID userId = UUID.fromString(record.getId());

            KeycloakAuthService.KeycloakUserRecord kcUser = keycloakAuthService.getUserById(userId);

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

        return ResponseEntity.ok(result);
    }

    @Override
    @AdminOnly
    @Transactional
    public ResponseEntity<Void> updateUser(String userId, UpdateUserRequest updateUserRequest) {
        if (userId == null || updateUserRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(User.USER).where(User.USER.ID.eq(id.toString()))
        );

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        // Keycloak-Update
        try {
            keycloakAuthService.updateUser(id, updateUserRequest);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getRawStatusCode()).build();
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UserNotificationResource>> getNotifications(UUID userId) {
        boolean userIdExists = dsl.fetchExists(
                dsl.selectOne().from(User.USER).where(User.USER.ID.eq(userId.toString()))
        );

        if (!userIdExists) {
            return ResponseEntity.notFound().build();
        }

        var notifications = dsl
                .select(
                        Notification.NOTIFICATION.ID,
                        Notification.NOTIFICATION.APPLICATION_ID,
                        Notification.NOTIFICATION.USER_ID,
                        Notification.NOTIFICATION.DATE,
                        Notification.NOTIFICATION.MESSAGE,
                        Notification.NOTIFICATION.READ_AT.isNotNull().as("isRead")
                )
                .from(Notification.NOTIFICATION)
                .where(Notification.NOTIFICATION.USER_ID.eq(userId.toString()))
                .fetchInto(UserNotificationResource.class);

        return ResponseEntity.ok(notifications);
    }

    @Override
    public ResponseEntity<Void> updateNotification(UUID id) {
        boolean notificationExists = dsl.fetchExists(
                dsl.selectOne().from(Notification.NOTIFICATION).where(Notification.NOTIFICATION.ID.eq(id))
        );

        if (!notificationExists) {
            return ResponseEntity.notFound().build();
        }

        dsl.update(Notification.NOTIFICATION)
                .set(Notification.NOTIFICATION.READ_AT, LocalDateTime.now(Clock.systemUTC()))
                .where(Notification.NOTIFICATION.ID.eq(id))
                .execute();

        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("@preferencesAuthorization.canAccessPreferences(authentication, #userId)")
    public ResponseEntity<NotificationPreferencesResource> getNotificationPreferences(UUID userId) {
        var result = dsl.select()
                .from(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .where(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID.eq(userId.toString()))
                .fetchOneInto(NotificationPreferencesRecord.class);

        NotificationPreferencesResource apiResource = new NotificationPreferencesResource();
        RecordToResourceMapperUtil.mapNotificationPreferencesRecordToResource(result, apiResource);

        return result != null ?
                ResponseEntity.ok(apiResource) :
                ResponseEntity.notFound().build();
    }

    @Override
    @PreAuthorize("@preferencesAuthorization.canAccessPreferences(authentication, #userId)")
    public ResponseEntity<NotificationPreferencesResource> updateNotificationPreferences(UUID userId, NotificationPreferencesUpdate notificationPreferencesUpdate) {
        if (userId == null || notificationPreferencesUpdate == null || notificationPreferencesUpdate.getNotificationWay() == null) {
            return ResponseEntity.badRequest().build();
        }

        var updatedRecord = dsl.update(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY, (NotificationWay) EnumMapperUtil.getPendantFromEnum(notificationPreferencesUpdate.getNotificationWay()))
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION, notificationPreferencesUpdate.getApplicationUpdatesNotification())
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION,  notificationPreferencesUpdate.getLicenseRenewalNotification())
                .where(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID.eq(userId.toString()))
                .returning()
                .fetchOneInto(NotificationPreferencesRecord.class);

        if (updatedRecord != null) {
            NotificationPreferencesResource apiResource = new NotificationPreferencesResource();
            RecordToResourceMapperUtil.mapNotificationPreferencesRecordToResource(updatedRecord, apiResource);
            return ResponseEntity.ok(apiResource);
        }

        return ResponseEntity.notFound().build();
    }
}
