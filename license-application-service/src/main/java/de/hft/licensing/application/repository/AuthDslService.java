package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.PasswordResetToken;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import de.hft.licensing.model.NotificationPreferencesUpdate;
import de.hft.licensing.model.NotificationWayApiEnum;
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.DSLContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthDslService {

    private final DSLContext dsl;

    public AuthDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void ensureLocalUserAndDefaults(UUID userId) {
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
        } catch (Exception ignored) {}
    }

    public boolean storePasswordResetToken(UUID userId, String token, LocalDateTime expiresAt, LocalDateTime createdAt) {
        try {
            int inserted = dsl.insertInto(PasswordResetToken.PASSWORD_RESET_TOKEN)
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.USER_ID, userId.toString())
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN, token)
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.EXPIRES_AT, expiresAt)
                    .set(PasswordResetToken.PASSWORD_RESET_TOKEN.CREATED_AT, createdAt)
                    .execute();
            return inserted > 0;
        } catch (DataIntegrityViolationException e) {
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

    public NotificationPreferencesRecord updateNotificationPreferences(UUID userId, NotificationPreferencesUpdate update) {
        return dsl.update(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(
                        NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY,
                        (NotificationWay) EnumMapperUtil.getPendantFromEnum(update.getNotificationWay())
                )
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION,
                        update.getApplicationUpdatesNotification())
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION,
                        update.getLicenseRenewalNotification())
                .where(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID.eq(userId.toString()))
                .returning()
                .fetchOneInto(NotificationPreferencesRecord.class);
    }
}