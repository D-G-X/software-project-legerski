package de.hft.licensing.services;

import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.LicenseTypeApiEnum;
import de.hft.licensing.services.dslService.BallotDslService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionAlgorithmServiceTest {


    @Mock
    private BallotDslService dslService;

    private DistributionAlgorithmService service;

    @BeforeEach
    void setUp() {
         service = new DistributionAlgorithmService(dslService);
    }

    @Test
    void returnsNull_whenBallotPeriodNotFound() {
        int periodId = 1;

        when(dslService.getBallotPeriodById(periodId)).thenReturn(null);

        var result = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), 10);

        assertNull(result);
        verify(dslService, never()).getCandidateApplications(any(), any(), any());
    }

    @Test
    void returnsNull_whenPeriodNotOverYet() {
        int periodId = 2;

        BallotPeriodRecord period = new BallotPeriodRecord();
        period.setStartDate(LocalDateTime.now().minusDays(1));
        period.setEndDate(LocalDateTime.now().plusMinutes(10));

        when(dslService.getBallotPeriodById(periodId)).thenReturn(period);

        var result = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), 10);

        assertNull(result);
        verify(dslService, never()).getCandidateApplications(any(), any(), any());
    }

    @Test
    void returnsEmptyLists_whenNoCandidates() {
        int periodId = 3;

        stubFinishedPeriod(periodId);
        when(dslService.getCandidateApplications(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(List.of());

        var result = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), 10);

        assertNotNull(result);
        assertTrue(result.selectedApplications().isEmpty());
        assertTrue(result.notSelectedApplications().isEmpty());

        verify(dslService, never()).updateApplicationInBallotTableToSelected(anyInt(), any());
        verify(dslService, never()).createLicenseForApplication(any(), any());
        verify(dslService, never()).updateApplicationStatusToApproved(any());
    }

    @Test
    void respectsMaxAcceptedOverride_andWritesDbForSelected() {
        int periodId = 4;

        stubFinishedPeriod(periodId);

        List<ApplicationRecord> candidates = List.of(
                app(1, "u1"),
                app(2, "u2"),
                app(3, "u3"),
                app(4, "u4"),
                app(5, "u5")
        );
        when(dslService.getCandidateApplications(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(candidates);

        int maxAccepted = 3;
        var result = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), maxAccepted);

        assertNotNull(result);
        assertEquals(3, result.selectedApplications().size());
        assertEquals(2, result.notSelectedApplications().size());

        verify(dslService, times(maxAccepted)).updateApplicationInBallotTableToSelected(eq(periodId), any());
        verify(dslService, times(maxAccepted)).createLicenseForApplication(any(), any());
        verify(dslService, times(maxAccepted)).updateApplicationStatusToApproved(any());
        verify(dslService, times(candidates.size() - maxAccepted)).updateApplicationStatusToRejected(any());

        long distinctSelectedIds = result.selectedApplications().stream()
                .map(ApplicationRecord::getId)
                .distinct()
                .count();
        assertEquals(result.selectedApplications().size(), distinctSelectedIds);
    }

    @Test
    void fairnessTwoUsers_maxAccepted4_resultsInTwoEach() {
        int periodId = 5;

        stubFinishedPeriod(periodId);

        List<ApplicationRecord> candidates = List.of(
                app(1, "A"), app(2, "A"), app(3, "A"),
                app(4, "B"), app(5, "B"), app(6, "B")
        );
        when(dslService.getCandidateApplications(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(candidates);

        int maxAccepted = 4;
        var result = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), maxAccepted);

        assertNotNull(result);
        assertEquals(4, result.selectedApplications().size());
        assertEquals(2, result.notSelectedApplications().size());

        Map<String, Long> countsByUser = result.selectedApplications().stream()
                .collect(Collectors.groupingBy(ApplicationRecord::getUserId, Collectors.counting()));

        assertEquals(2L, countsByUser.get("A"));
        assertEquals(2L, countsByUser.get("B"));

        verify(dslService, times(maxAccepted)).updateApplicationInBallotTableToSelected(eq(periodId), any());
        verify(dslService, times(maxAccepted)).createLicenseForApplication(any(), any());
        verify(dslService, times(maxAccepted)).updateApplicationStatusToApproved(any());
        verify(dslService, times(candidates.size() - maxAccepted)).updateApplicationStatusToRejected(any());
    }



    private void stubFinishedPeriod(int periodId) {
        BallotPeriodRecord period = new BallotPeriodRecord();
        period.setStartDate(LocalDateTime.now().minusDays(10));
        period.setEndDate(LocalDateTime.now().minusDays(1));

        when(dslService.getBallotPeriodById(periodId)).thenReturn(period);
    }


    private static ApplicationRecord app(int id, String userId) {
        ApplicationRecord r = new ApplicationRecord();
        r.setId(id);
        r.setUserId(userId);
        return r;
    }

    private static LicenseTypeApiEnum anyLicenseType() {
        return LicenseTypeApiEnum.values()[0];
    }
}