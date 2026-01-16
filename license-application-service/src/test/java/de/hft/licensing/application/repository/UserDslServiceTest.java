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
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DefaultDSLContext dsl;

    private UserDslService service;

    @BeforeEach
    void setUp() {
        service = new UserDslService(dsl);
    }

    @Test
    void userExists_uuid_true() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.userExists(UUID.randomUUID()));
    }

    @Test
    void userExists_string_false() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(false);
        assertFalse(service.userExists("x"));
    }

    @Test
    void listUsers_returnsList() {
        List<UserRecord> list = List.of(new UserRecord());
        when(dsl.selectFrom(User.USER)
                .offset(0)
                .limit(2)
                .fetchInto(UserRecord.class)).thenReturn(list);

        assertSame(list, service.listUsers(0, 2));
    }

    @Test
    void createUser_executes() {
        when(dsl.insertInto(User.USER)
                .set(eq(User.USER.ID), eq("u"))
                .execute()).thenReturn(1);

        assertEquals(1, service.createUser("u"));
    }

    @Test
    void setApplicationStatusToCancelled_executes() {
        when(dsl.update(de.hft.licensing.db.tables.Application.APPLICATION)
                .set(eq(de.hft.licensing.db.tables.Application.APPLICATION.APPLICATION_STATUS), (ApplicationStatus) any())
                .where((Condition) any())
                .execute()).thenReturn(3);

        assertEquals(3, service.setApplicationStatusToCancelled("u"));
    }

    @Test
    void getAllUserIdCellsInDb_returnsResult() {
        Field<String> s = DSL.field(DSL.name("table_schema"), String.class);
        Field<String> t = DSL.field(DSL.name("table_name"), String.class);
        Field<String> u = DSL.field(DSL.name("udt_name"), String.class);

        @SuppressWarnings("unchecked")
        Result<Record3<String, String, String>> r = (Result<Record3<String, String, String>>) mock(Result.class);

        when(dsl.select(any(Field.class), any(Field.class), any(Field.class))
                .from(any(TableLike.class))
                .where(any(Condition.class))
                .and(any(Condition.class))
                .and(any(Condition.class))
                .fetch()).thenReturn(r);

        assertSame(r, service.getAllUserIdCellsInDb(s, t, u));
    }

    @Test
    void updateTableFieldsWithNewValue_executes_string() {
        Table<?> table = DSL.table(DSL.name("t"));
        Field<String> col = DSL.field(DSL.name("user_id"), String.class);

        when(dsl.update(eq(table))
                .set(eq(col), eq("new"))
                .where(any(Condition.class))
                .execute()).thenReturn(1);

        service.updateTableFieldsWithNewValue(table, col, "old", "new");
    }

    @Test
    void deleteUser_executes() {
        when(dsl.deleteFrom(User.USER)
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.deleteUser("u"));
    }

    @Test
    void getNotifications_returnsList() {
        List<UserNotificationResource> list = List.of(new UserNotificationResource());

        when(dsl.select(any(), any(), any(), any(), any(), any())
                .from(Notification.NOTIFICATION)
                .where((Condition) any())
                .fetchInto(UserNotificationResource.class)).thenReturn(list);

        assertSame(list, service.getNotifications(UUID.randomUUID()));
    }

    @Test
    void notificationExists_true() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.notificationExists(UUID.randomUUID()));
    }

    @Test
    void markNotificationRead_executes() {
        when(dsl.update(Notification.NOTIFICATION)
                .set(eq(Notification.NOTIFICATION.READ_AT), any(LocalDateTime.class))
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.markNotificationRead(UUID.randomUUID(), LocalDateTime.now()));
    }

    @Test
    void getNotificationPreferences_returnsRecord() {
        NotificationPreferencesRecord rec = new NotificationPreferencesRecord();

        when(dsl.selectFrom(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .where((Condition) any())
                .fetchOneInto(NotificationPreferencesRecord.class)).thenReturn(rec);

        assertSame(rec, service.getNotificationPreferences(UUID.randomUUID()));
    }

    @Test
    void updateNotificationPreferences_returnsRecord() {
        NotificationPreferencesUpdate upd = new NotificationPreferencesUpdate();
        upd.setNotificationWay(NotificationWayApiEnum.NONE);
        upd.setApplicationUpdatesNotification(true);
        upd.setLicenseRenewalNotification(false);

        NotificationPreferencesRecord rec = new NotificationPreferencesRecord();

        when(dsl.update(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY), any(NotificationWay.class))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION), eq(true))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION), eq(false))
                .where((Condition) any())
                .returning()
                .fetchOneInto(NotificationPreferencesRecord.class)).thenReturn(rec);

        assertSame(rec, service.updateNotificationPreferences(UUID.randomUUID(), upd));
    }

    @Test
    void createInitialUserNotificationPreferences_executes() {
        when(dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID), eq("u"))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY), any(NotificationWay.class))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION), eq(true))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION), eq(true))
                .execute()).thenReturn(1);

        assertEquals(1, service.createInitialUserNotificationPreferences("u"));
    }
}