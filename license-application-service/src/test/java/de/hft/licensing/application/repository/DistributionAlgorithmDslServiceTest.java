package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.LicenseTypeApiEnum;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributionAlgorithmDslServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DefaultDSLContext dsl;

    private DistributionAlgorithmDslService service;

    @BeforeEach
    void setUp() {
        service = new DistributionAlgorithmDslService(dsl);
    }

    @Test
    void updateApplicationInBallotTableToSelected_executes() {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(10);

        when(dsl.update(Ballot.BALLOT)
                .set(eq(Ballot.BALLOT.SELECTED), eq(true))
                .where((Condition) any())
                .and((Condition) any())
                .execute()).thenReturn(1);

        service.updateApplicationInBallotTableToSelected(5, app);
    }

    @Test
    void updateApplicationStatusToApproved_executes() {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(1);

        when(dsl.update(Application.APPLICATION)
                .set(eq(Application.APPLICATION.APPLICATION_STATUS), eq(ApplicationStatus.approved))
                .where((Condition) any())
                .execute()).thenReturn(1);

        service.updateApplicationStatusToApproved(app);
    }

    @Test
    void updateApplicationStatusToRejected_executes() {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(1);

        when(dsl.update(Application.APPLICATION)
                .set(eq(Application.APPLICATION.APPLICATION_STATUS), eq(ApplicationStatus.rejected))
                .where((Condition) any())
                .execute()).thenReturn(1);

        service.updateApplicationStatusToRejected(app);
    }

    @Test
    void createLicenseForApplication_executes() {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(1);
        app.setUserId("u");

        when(dsl.insertInto(License.LICENSE)
                .set(eq(License.LICENSE.USER_ID), eq("u"))
                .set(eq(License.LICENSE.APPLICATION_ID), eq(1))
                .set(eq(License.LICENSE.LICENSE_TYPE), (LicenseType) any())
                .set(eq(License.LICENSE.LICENSE_STATUS), (LicenseStatus) any())
                .set(eq(License.LICENSE.ISSUED_AT), (LocalDateTime) any())
                .set(eq(License.LICENSE.EXPIRES_AT), (LocalDateTime) any())
                .execute()).thenReturn(1);

        assertEquals(1, service.createLicenseForApplication(app, LicenseTypeApiEnum.values()[0]));
    }

    @Test
    void getCandidateApplications_withoutLicenseType() {
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .where(any(Condition.class))
                .fetchInto(ApplicationRecord.class)).thenReturn(list);

        var out = service.getCandidateApplications(LocalDateTime.now().minusDays(1), LocalDateTime.now(), null);
        assertEquals(1, out.size());
    }

    @Test
    void getCandidateApplications_withLicenseType() {
        List<ApplicationRecord> list = List.of(new ApplicationRecord());

        when(dsl.selectFrom(Application.APPLICATION)
                .where(any(Condition.class))
                .fetchInto(ApplicationRecord.class)).thenReturn(list);

        var out = service.getCandidateApplications(LocalDateTime.now().minusDays(1), LocalDateTime.now(), LicenseTypeApiEnum.values()[0]);
        assertEquals(1, out.size());
    }

    @Test
    void getBallotPeriodById_returnsRecord() {
        BallotPeriodRecord rec = new BallotPeriodRecord();
        when(dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where((Condition) any())
                .fetchOneInto(BallotPeriodRecord.class)).thenReturn(rec);

        assertSame(rec, service.getBallotPeriodById(1));
    }

    @Test
    void deleteApplications_executesLoop() {
        ApplicationRecord a1 = new ApplicationRecord(); a1.setId(1);
        ApplicationRecord a2 = new ApplicationRecord(); a2.setId(2);
        service.deleteApplications(List.of(a1, a2));
    }


    @Test
    @SuppressWarnings({"rawtypes","unchecked"})
    void getCurrentBallotPeriodId_returnsId_whenPresent() {
        var rec = new BallotPeriodRecord();
        rec.setId(7);
        rec.setStartDate(LocalDateTime.now().minusDays(1));
        rec.setEndDate(LocalDateTime.now().plusDays(1));

        SelectWhereStep whereStep = mock(SelectWhereStep.class);
        SelectConditionStep condStep = mock(SelectConditionStep.class);

        when(dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)).thenReturn(whereStep);
        when(whereStep.where(any(Condition.class))).thenReturn(condStep);
        when(condStep.and(any(Condition.class))).thenReturn(condStep);

        when(condStep.fetchOne()).thenReturn(rec);

        assertEquals(7, service.getCurrentBallotPeriodId());
    }
}