package de.hft.licensing.rest;

import de.hft.licensing.api.UsersApi;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.model.*;
import de.hft.licensing.services.KeycloakAuthService;
import de.hft.licensing.services.auth.AdminOnly;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UsersController implements UsersApi {

    private final DSLContext dsl;
    private final KeycloakAuthService keycloakAuthService;

    public UsersController(DSLContext dsl, KeycloakAuthService keycloakAuthService) {
        this.keycloakAuthService = keycloakAuthService;
        this.dsl = dsl;

    }

    @Override
    @AdminOnly
    public ResponseEntity<Void> createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null || createUserRequest.getSchema() == null || createUserRequest.getSchema().getUsername() == null) {
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
                System.out.println("[WARNING] - Failed to insert notification preferences for user ID " + id + " into the local database.");
            } else {
                System.out.println("[INFO] - Notification preferences for user ID " + id + " created successfully in the local database.");
            }

            if (insertedUser > 0) {
                return ResponseEntity.created(URI.create("/users/" + id)).build();
            } else {
                return ResponseEntity.status(500).build();
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @Override
    @AdminOnly
    public ResponseEntity<Void> deleteUser(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId.toString()))
        );
        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        boolean deletedInKeycloak;
        try {
            deletedInKeycloak = keycloakAuthService.deleteUserInKeycloak(userId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(502).build();
        }

        if (!deletedInKeycloak) {
            return ResponseEntity.notFound().build();
        }

        int deletedRows = dsl.deleteFrom(User.USER)
                .where(User.USER.ID.eq(userId.toString()))
                .execute();

        if (deletedRows == 0) {
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    @AdminOnly
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
        return null;
    }

    @Override
    public ResponseEntity<Void> updateNotifications(Integer id) {
        return null;
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
