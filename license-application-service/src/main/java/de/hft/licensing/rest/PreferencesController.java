package de.hft.licensing.rest;

import de.hft.licensing.api.PreferencesApi;
import de.hft.licensing.model.NotificationPreferencesResource;
import de.hft.licensing.model.NotificationPreferencesUpdate;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class PreferencesController implements PreferencesApi {

    private final DSLContext dsl;

    public PreferencesController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @PreAuthorize("@preferencesAuthorization.canAccessPreferences(authentication, #userId)")
    public ResponseEntity<NotificationPreferencesResource> getNotificationPreferences(UUID userId) {
        return null;
    }

    @Override
    public ResponseEntity<NotificationPreferencesResource> updateNotificationPreferences(UUID userId, NotificationPreferencesUpdate notificationPreferencesUpdate) {
        return null;
    }
}
