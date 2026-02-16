package de.hft.licensing.application.restController;

import de.hft.licensing.api.UsersApi;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.application.services.UserService;
import de.hft.licensing.application.services.authServices.AdminOnly;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UsersController implements UsersApi {

    private final UserService userService;
    private final Logger log = LicensingLoggerFactory.getLogger(UsersController.class);

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    // ONLY FOR ADMINISTRATION PURPOSES - DO NOT USE IN PRODUCTION
    @Override
    @AdminOnly
    public ResponseEntity<Void> createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null || createUserRequest.getSchema() == null || createUserRequest.getSchema().getUsername() == null) {
            log.error("Invalid create user request received.");
            return ResponseEntity.badRequest().build();
        }

        try {
            String id = userService.createUserWithInitialPreferences();
            return ResponseEntity.created(URI.create("/users/" + id)).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }


    @Override
    @PreAuthorize("@userAuthorization.canAccessUser(authentication, #userId)")
    public ResponseEntity<Void> deleteUser(UUID userId) {
        if (userId == null) {
            log.error("Invalid user ID provided for deletion.");
            return ResponseEntity.badRequest().build();
        }

        UserService.DeleteUserResult res = userService.deleteUser(userId);

        if (res == UserService.DeleteUserResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        if (res == UserService.DeleteUserResult.KEYCLOAK_ERROR) {
            return ResponseEntity.status(502).build();
        }
        if (res == UserService.DeleteUserResult.INTERNAL_ERROR) {
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.noContent().build();
    }


    @Override
    @PreAuthorize("@userAuthorization.canAccessUser(authentication, #userId)")
    public ResponseEntity<UserResource> getUser(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        UserResource user = userService.getUser(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<UserResource>> listUsers(String username, String email, Integer first, Integer max) {
        int offset = (first == null || first < 0) ? 0 : first;
        int limit = (max == null || max <= 0) ? 100 : Math.min(max, 100);

        List<UserResource> result = userService.listUsers(offset, limit);
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

        try {
            UserService.UpdateUserResult res = userService.updateUser(id, updateUserRequest);
            if (res == UserService.UpdateUserResult.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            if (res == UserService.UpdateUserResult.OK) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(500).build();
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getRawStatusCode()).build();
        }
    }


    @Override
    public ResponseEntity<List<UserNotificationResource>> getNotifications(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<UserNotificationResource> notifications = userService.getNotifications(userId);
        if (notifications == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notifications);
    }

    @Override
    public ResponseEntity<Void> updateNotification(UUID id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean updated = userService.markNotificationRead(id);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }


    @Override
    @PreAuthorize("@preferencesAuthorization.canAccessPreferences(authentication, #userId)")
    public ResponseEntity<NotificationPreferencesResource> getNotificationPreferences(UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        NotificationPreferencesResource resource = userService.getNotificationPreferences(userId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resource);
    }

    @Override
    @PreAuthorize("@preferencesAuthorization.canAccessPreferences(authentication, #userId)")
    public ResponseEntity<NotificationPreferencesResource> updateNotificationPreferences(UUID userId, NotificationPreferencesUpdate notificationPreferencesUpdate) {
        if (userId == null || notificationPreferencesUpdate == null || notificationPreferencesUpdate.getNotificationWay() == null) {
            return ResponseEntity.badRequest().build();
        }

        NotificationPreferencesResource resource = userService.updateNotificationPreferences(userId, notificationPreferencesUpdate);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resource);
    }

    @Override
    @PreAuthorize("@userAuthorization.canAccessUser(authentication, #userId)")
    public ResponseEntity<Void> changePasswordForUser(UUID userId, ChangePasswordForUserRequest changePasswordRequest) {
        if (userId == null || changePasswordRequest == null || changePasswordRequest.getNewPassword() == null || changePasswordRequest.getOldPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        UserService.ChangePasswordResult res = userService.changePassword(userId, changePasswordRequest);

        if (res == UserService.ChangePasswordResult.USER_NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        if (res == UserService.ChangePasswordResult.OLD_PASSWORD_WRONG) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (res == UserService.ChangePasswordResult.NEW_PASSWORD_INVALID) {
            return ResponseEntity.badRequest().build();
        }
        if (res == UserService.ChangePasswordResult.KEYCLOAK_NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
