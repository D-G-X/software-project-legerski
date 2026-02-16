package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DSLContext dsl;

    private ApplicationDslService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationDslService(dsl);
    }

    @Test
    void isBallotPeriodActive_returnsDslFetchExists() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.isBallotPeriodActive(LocalDateTime.now()));
        verify(dsl).fetchExists(any(Select.class));
    }

    @Test
    void getCurrentBallotPeriodId_returnsId() {
        when(dsl.select(BallotPeriod.BALLOT_PERIOD.ID)
                .from(BallotPeriod.BALLOT_PERIOD)
                .where((Condition) any())
                .and((Condition) any())
                .fetchOneInto(Integer.class))
                .thenReturn(7);

        assertEquals(7, service.getCurrentBallotPeriodId(LocalDateTime.now()));
    }

    @Test
    void userExists_returnsDslFetchExists() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(false);
        assertFalse(service.userExists(UUID.randomUUID()));
        verify(dsl).fetchExists(any(Select.class));
    }

    @Test
    void createApplication_insertsAndReturnsRecord() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(1);

        when(dsl.insertInto(Application.APPLICATION)
                .set(eq(Application.APPLICATION.USER_ID), eq(userId.toString()))
                .set(eq(Application.APPLICATION.APPLICATION_STATUS), eq(ApplicationStatus.draft))
                .set(eq(Application.APPLICATION.APPLIED_AT), eq(now))
                .set(eq(Application.APPLICATION.CHANGED_AT), eq(now))
                .set(eq(Application.APPLICATION.CADASTRAL_REFERENCE), eq("cad"))
                .set(eq(Application.APPLICATION.LICENSE_TYPE), eq(LicenseType.etv))
                .set(eq(Application.APPLICATION.REMARKS), eq("r"))
                .set(eq(Application.APPLICATION.VERIFICATION_STATUS), eq(VerificationStatus.pending))
                .returning()
                .fetchOneInto(ApplicationRecord.class))
                .thenReturn(rec);

        assertSame(rec, service.createApplication(userId, "cad", LicenseType.etv, "r", now));
    }

    @Test
    void createBallotEntry_insertsAndExecutes() {
        when(dsl.insertInto(Ballot.BALLOT)
                .set(eq(Ballot.BALLOT.BALLOT_PERIOD_ID), eq(1))
                .set(eq(Ballot.BALLOT.APPLICATION_ID), eq(2))
                .set(eq(Ballot.BALLOT.SELECTED), eq(false))
                .execute())
                .thenReturn(1);

        assertEquals(1, service.createBallotEntry(1, 2));
    }

    @Test
    void deleteApplication_deletesAndExecutes() {
        when(dsl.deleteFrom(Application.APPLICATION)
                .where((Condition) any())
                .execute())
                .thenReturn(1);

        assertEquals(1, service.deleteApplication(10));
    }

    @Test
    void getApplication_selectsAndFetchesIntoRecord() {
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(10);

        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchOneInto(ApplicationRecord.class))
                .thenReturn(rec);

        assertSame(rec, service.getApplication(10));
    }

    @Test
    void listApplications_userNull_statusNull_fetchAll() {
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .fetchInto(ApplicationRecord.class))
                .thenReturn(list);

        assertSame(list, service.listApplications(null, null));
    }

    @Test
    void listApplications_userNotNull_statusNull_filtersByUser() {
        UUID userId = UUID.randomUUID();
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchInto(ApplicationRecord.class))
                .thenReturn(list);

        assertSame(list, service.listApplications(userId, null));
    }

    @Test
    void listApplications_userNull_statusNotNull_filtersByStatus() {
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchInto(ApplicationRecord.class))
                .thenReturn(list);

        assertSame(list, service.listApplications(null, ApplicationStatus.approved));
    }

    @Test
    void listApplications_userNotNull_statusNotNull_filtersByUserAndStatus() {
        UUID userId = UUID.randomUUID();
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .where((Condition) any())
                .fetchInto(ApplicationRecord.class))
                .thenReturn(list);

        assertSame(list, service.listApplications(userId, ApplicationStatus.approved));
    }

    @Test
    void getApplicationStatus_fetchesOneIntoEnum() {
        when(dsl.select(Application.APPLICATION.APPLICATION_STATUS)
                .from(Application.APPLICATION)
                .where((Condition) any())
                .fetchOneInto(ApplicationStatus.class))
                .thenReturn(ApplicationStatus.payment_received);

        assertEquals(ApplicationStatus.payment_received, service.getApplicationStatus(1));
    }

    @Test
    void getApplicationRemarks_fetchesOneIntoString() {
        when(dsl.select(Application.APPLICATION.REMARKS)
                .from(Application.APPLICATION)
                .where((Condition) any())
                .fetchOneInto(String.class))
                .thenReturn("x");

        assertEquals("x", service.getApplicationRemarks(1));
    }

    @Test
    void updateApplication_updatesAndReturnsRecord() {
        ApplicationRecord rec = new ApplicationRecord();
        rec.setId(9);

        Map<Field<?>, Object> updates = Map.of(Application.APPLICATION.REMARKS, "r");

        when(dsl.update(Application.APPLICATION)
                .set(eq(updates))
                .where((Condition) any())
                .returning()
                .fetchOneInto(ApplicationRecord.class))
                .thenReturn(rec);

        assertSame(rec, service.updateApplication(9, updates));
    }

    @Test
    void isBallotPeriodActive_canReturnFalse() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(false);
        assertFalse(service.isBallotPeriodActive(LocalDateTime.now()));
    }

    @Test
    void userExists_canReturnTrue() {
        when(dsl.fetchExists(any(Select.class))).thenReturn(true);
        assertTrue(service.userExists(UUID.randomUUID()));
    }
}