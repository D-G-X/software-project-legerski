package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.NotificationWay;
import de.hft.licensing.db.tables.NotificationPreferences;
import de.hft.licensing.db.tables.PasswordResetToken;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private AuthDslService service;

    @BeforeEach
    void setUp() {
        service = new AuthDslService(dsl);
    }

    @Test
    void ensureLocalUserAndDefaults_swallowExceptions() {
        UUID userId = UUID.randomUUID();

        when(dsl.insertInto(User.USER)
                .set(eq(User.USER.ID), eq(userId.toString()))
                .execute()).thenThrow(new DataIntegrityViolationException("dup"));

        when(dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID), eq(userId.toString()))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY), (NotificationWay) any())
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION), eq(true))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION), eq(true))
                .execute()).thenThrow(new RuntimeException("fail"));

        assertDoesNotThrow(() -> service.ensureLocalUserAndDefaults(userId));
    }

    @Test
    void ensureLocalUserAndDefaults_successPath() {
        UUID userId = UUID.randomUUID();

        when(dsl.insertInto(User.USER)
                .set(eq(User.USER.ID), eq(userId.toString()))
                .execute()).thenReturn(1);

        when(dsl.insertInto(NotificationPreferences.NOTIFICATION_PREFERENCES)
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.USER_ID), eq(userId.toString()))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.NOTIFICATION_WAY), (NotificationWay) any())
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.APPLICATION_UPDATES_NOTIFICATION), eq(true))
                .set(eq(NotificationPreferences.NOTIFICATION_PREFERENCES.LICENSE_RENEWAL_NOTIFICATION), eq(true))
                .execute()).thenReturn(1);

        assertDoesNotThrow(() -> service.ensureLocalUserAndDefaults(userId));
    }

    @Test
    void storePasswordResetToken_returnsTrue_whenInserted() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(dsl.insertInto(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.USER_ID), eq(userId.toString()))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN), eq("tok"))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.EXPIRES_AT), eq(now.plusMinutes(10)))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.CREATED_AT), eq(now))
                .execute()).thenReturn(1);

        assertTrue(service.storePasswordResetToken(userId, "tok", now.plusMinutes(10), now));
    }

    @Test
    void storePasswordResetToken_returnsFalse_onDataIntegrityViolation() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(dsl.insertInto(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.USER_ID), eq(userId.toString()))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.TOKEN), eq("tok"))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.EXPIRES_AT), eq(now.plusMinutes(10)))
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.CREATED_AT), eq(now))
                .execute()).thenThrow(new DataIntegrityViolationException("dup"));

        assertFalse(service.storePasswordResetToken(userId, "tok", now.plusMinutes(10), now));
    }


    @Test
    void getPasswordResetTokenRecord_delegates() {
        PasswordResetTokenRecord rec = new PasswordResetTokenRecord();
        rec.setToken("tok");

        // Step-Mocks statt Deep-Stubs
        SelectWhereStep<PasswordResetTokenRecord> whereStep = mock(SelectWhereStep.class);
        SelectConditionStep<PasswordResetTokenRecord> conditionStep = mock(SelectConditionStep.class);

        when(dsl.selectFrom(PasswordResetToken.PASSWORD_RESET_TOKEN)).thenReturn(whereStep);
        when(whereStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(rec);

        assertSame(rec, service.getPasswordResetTokenRecord("tok"));
    }

    @Test
    void markPasswordResetTokenAsUsed_true_whenUpdated() {
        when(dsl.update(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.USED), eq(true))
                .where((Condition) any())
                .execute()).thenReturn(1);

        assertTrue(service.markPasswordResetTokenAsUsed("tok"));
    }

    @Test
    void markPasswordResetTokenAsUsed_false_whenNotUpdated() {
        when(dsl.update(PasswordResetToken.PASSWORD_RESET_TOKEN)
                .set(eq(PasswordResetToken.PASSWORD_RESET_TOKEN.USED), eq(true))
                .where((Condition) any())
                .execute()).thenReturn(0);

        assertFalse(service.markPasswordResetTokenAsUsed("tok"));
    }
}