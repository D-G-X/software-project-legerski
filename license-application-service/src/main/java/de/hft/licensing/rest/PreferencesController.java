package de.hft.licensing.rest;

import de.hft.licensing.api.PreferencesApi;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.model.NotificationPreferencesResource;
import de.hft.licensing.model.NotificationPreferencesUpdate;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
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
