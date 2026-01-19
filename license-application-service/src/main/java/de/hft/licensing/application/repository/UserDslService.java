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
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

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

    public Result<Record3<String, String, String>> getAllUserIdCellsInDb (Field<String> TABLE_SCHEMA, Field<String> TABLE_NAME, Field<String> UDT_NAME) {
        return dsl.select(TABLE_SCHEMA, TABLE_NAME, UDT_NAME)
                .from(table(name("information_schema", "columns")))
                .where(field(name("column_name"), String.class).eq("user_id"))
                .and(field(name("table_schema"), String.class).notIn("pg_catalog", "information_schema"))
                .and(not(TABLE_SCHEMA.eq("public").and(TABLE_NAME.eq("user"))))
                .fetch();
    }

    public <T> void updateTableFieldsWithNewValue (Table<?> table, Field<T> col, T oldUserId, T newId) {
         dsl.update(table)
                .set(col, newId)
                .where(col.eq(oldUserId))
                .execute();
    }

    public int deleteUser(String userId) {
        return dsl.deleteFrom(User.USER)
                .where(User.USER.ID.eq(userId))
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
                        Notification.NOTIFICATION.READ_AT.isNotNull()
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

    public int createNotification(int applicationId, UUID userId, String message) {
        return dsl.insertInto(Notification.NOTIFICATION)
                .set(Notification.NOTIFICATION.APPLICATION_ID, applicationId)
                .set(Notification.NOTIFICATION.USER_ID, userId.toString())
                .set(Notification.NOTIFICATION.MESSAGE, message)
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