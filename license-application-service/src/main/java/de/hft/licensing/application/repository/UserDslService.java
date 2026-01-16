package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.Notification;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.NotificationPreferencesRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.model.NotificationPreferencesUpdate;
import de.hft.licensing.model.NotificationWayApiEnum;
import de.hft.licensing.model.UserNotificationResource;
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserDslService {

    private final DefaultDSLContext dsl;

    private static final ApplicationStatus CANCELLED = ApplicationStatus.cancelled;

    public UserDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean userExists(UUID userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId.toString()))
        );
    }

    public boolean userExists(String userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId))
        );
    }

    public List<UserRecord> listUsers(int offset, int limit) {
        return dsl.selectFrom(User.USER)
                .offset(offset)
                .limit(limit)
                .fetchInto(UserRecord.class);
    }

    public int createUser(String userId) {
        return dsl.insertInto(User.USER)
                .set(User.USER.ID, userId)
                .execute();
    }

    public int setApplicationStatusToCancelled(String userId) {
        return dsl.update(de.hft.licensing.db.tables.Application.APPLICATION)
                .set(de.hft.licensing.db.tables.Application.APPLICATION.APPLICATION_STATUS, CANCELLED)
                .where(de.hft.licensing.db.tables.Application.APPLICATION.USER_ID.eq(userId))
                .execute();
    }

    public List<UserNotificationResource> getNotifications(UUID userId) {
        return dsl
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
    }

    public boolean notificationExists(UUID notificationId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(Notification.NOTIFICATION)
                        .where(Notification.NOTIFICATION.ID.eq(notificationId))
        );
    }

    public int markNotificationRead(UUID notificationId, LocalDateTime readAt) {
        return dsl.update(Notification.NOTIFICATION)
                .set(Notification.NOTIFICATION.READ_AT, readAt)
                .where(Notification.NOTIFICATION.ID.eq(notificationId))
                .execute();
    }

    public NotificationPreferencesRecord getNotificationPreferences(UUID userId) {
        return dsl.selectFrom(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .where(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID.eq(userId.toString()))
                .fetchOneInto(NotificationPreferencesRecord.class);
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

    public int createInitialUserNotificationPreferences(String userId) {
        return dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID, userId)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY,
                        (NotificationWay) EnumMapperUtil.getPendantFromEnum(NotificationWayApiEnum.NONE))
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION, true)
                .set(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION, true)
                .execute();
    }
}