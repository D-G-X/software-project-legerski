package de.hft.licensing.application.services;

import de.hft.licensing.application.dto.PaymentRequestDto;
import de.hft.licensing.application.dto.PaymentResponseDto;
import de.hft.licensing.application.repository.BallotPeriodDslService;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.LicenseTypeApiEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BallotPeriodServiceTest {

    @Mock
    private BallotPeriodDslService repository;

    @Mock
    private DistributionAlgorithmService distributionAlgorithmService;

    @Mock
    private MockBankService mockBankClient;

    private BallotPeriodService service;

    @BeforeEach
    void setUp() {
        service = new BallotPeriodService(repository, distributionAlgorithmService, mockBankClient);
    }

    @Test
    void listBallotPeriods_delegatesToRepository() {
        List<BallotPeriodRecord> recs = List.of(new BallotPeriodRecord());
        when(repository.listBallotPeriods()).thenReturn(recs);

        assertSame(recs, service.listBallotPeriods());
        verify(repository).listBallotPeriods();
    }

    @Test
    void countApplicationsInPeriod_delegatesToRepository() {
        when(repository.countApplicationsInPeriod(5)).thenReturn(12);
        assertEquals(12, service.countApplicationsInPeriod(5));
    }

    @Test
    void getBallotPeriodById_delegatesToRepository() {
        BallotPeriodRecord r = new BallotPeriodRecord();
        when(repository.getBallotPeriodById(7)).thenReturn(r);

        assertSame(r, service.getBallotPeriodById(7));
    }

    @Test
    void getBallotPeriodEntries_delegatesToRepository() {
        List<ApplicationRecord> apps = List.of(app(1), app(2));
        when(repository.getBallotPeriodEntries(9)).thenReturn(apps);

        assertSame(apps, service.getBallotPeriodEntries(9));
    }

    @Test
    void getActiveBallotPeriodUtcNow_delegatesToRepository() {
        BallotPeriodRecord r = new BallotPeriodRecord();
        when(repository.getActiveBallotPeriod(any(LocalDateTime.class))).thenReturn(r);

        assertSame(r, service.getActiveBallotPeriodUtcNow());
        verify(repository).getActiveBallotPeriod(any(LocalDateTime.class));
    }

    @Test
    void createBallotPeriod_returnsOVERLAPS_whenOverlapsTrue() {
        LocalDateTime s = LocalDateTime.now().minusDays(1);
        LocalDateTime e = LocalDateTime.now();

        when(repository.overlaps(s, e)).thenReturn(true);

        var res = service.createBallotPeriod(s, e);

        assertEquals(BallotPeriodService.CreateBallotPeriodResultCode.OVERLAPS, res.code());
        assertNull(res.record());
        verify(repository, never()).createBallotPeriod(any(), any());
    }

    @Test
    void createBallotPeriod_returnsINTERNAL_ERROR_whenCreateReturnsNull() {
        LocalDateTime s = LocalDateTime.now().minusDays(1);
        LocalDateTime e = LocalDateTime.now();

        when(repository.overlaps(s, e)).thenReturn(false);
        when(repository.createBallotPeriod(s, e)).thenReturn(null);

        var res = service.createBallotPeriod(s, e);

        assertEquals(BallotPeriodService.CreateBallotPeriodResultCode.INTERNAL_ERROR, res.code());
        assertNull(res.record());
    }

    @Test
    void createBallotPeriod_returnsOK_whenCreated() {
        LocalDateTime s = LocalDateTime.now().minusDays(1);
        LocalDateTime e = LocalDateTime.now();

        when(repository.overlaps(s, e)).thenReturn(false);

        BallotPeriodRecord rec = new BallotPeriodRecord();
        when(repository.createBallotPeriod(s, e)).thenReturn(rec);

        var res = service.createBallotPeriod(s, e);

        assertEquals(BallotPeriodService.CreateBallotPeriodResultCode.OK, res.code());
        assertSame(rec, res.record());
    }

    @Test
    void runLottery_returnsNOT_FOUND_onIllegalArgumentException() {
        when(distributionAlgorithmService.runLotteryForBallotPeriod(anyInt(), any(), anyInt()))
                .thenThrow(new IllegalArgumentException("no period"));

        var res = service.runLotteryForBallotPeriod(1, anyLicenseType(), 10);

        assertEquals(BallotPeriodService.RunLotteryResultCode.NOT_FOUND, res.code());
        assertNull(res.selected());
        assertNull(res.notSelected());

        verify(repository, never()).markSelected(anyInt(), anyInt());
        verify(repository, never()).truncateApplicationPayments();
    }

    @Test
    void runLottery_returnsBAD_REQUEST_onIllegalStateException() {
        when(distributionAlgorithmService.runLotteryForBallotPeriod(anyInt(), any(), anyInt()))
                .thenThrow(new IllegalStateException("period not over"));

        var res = service.runLotteryForBallotPeriod(1, anyLicenseType(), 10);

        assertEquals(BallotPeriodService.RunLotteryResultCode.BAD_REQUEST, res.code());
        assertNull(res.selected());
        assertNull(res.notSelected());

        verify(repository, never()).truncateApplicationPayments();
    }

    @Test
    void runLottery_returnsINTERNAL_ERROR_onOtherException() {
        when(distributionAlgorithmService.runLotteryForBallotPeriod(anyInt(), any(), anyInt()))
                .thenThrow(new RuntimeException("boom"));

        var res = service.runLotteryForBallotPeriod(1, anyLicenseType(), 10);

        assertEquals(BallotPeriodService.RunLotteryResultCode.INTERNAL_ERROR, res.code());
        assertNull(res.selected());
        assertNull(res.notSelected());

        verify(repository, never()).truncateApplicationPayments();
    }

    @Test
    void runLottery_returnsBAD_REQUEST_whenLotteryNull() {
        when(distributionAlgorithmService.runLotteryForBallotPeriod(anyInt(), any(), anyInt()))
                .thenReturn(null);

        var res = service.runLotteryForBallotPeriod(1, anyLicenseType(), 10);

        assertEquals(BallotPeriodService.RunLotteryResultCode.BAD_REQUEST, res.code());
        assertNull(res.selected());
        assertNull(res.notSelected());

        verify(repository, never()).truncateApplicationPayments();
    }

    @Test
    @Disabled
    void runLottery_OK_marksSelected_processesPayments_updatesPaidAndUnpaid_andTruncates() {
        int periodId = 99;

        ApplicationRecord a1 = app(1);
        ApplicationRecord a2 = app(2);
        List<ApplicationRecord> selected = List.of(a1, a2);
        List<ApplicationRecord> notSelected = List.of(app(3));

        var lottery = new DistributionAlgorithmService.LotteryResult(selected, notSelected);

        when(distributionAlgorithmService.runLotteryForBallotPeriod(eq(periodId), any(), anyInt()))
                .thenReturn(lottery);

        // Payment record for app1 exists
        ApplicationPaymentRecord pay1 = new ApplicationPaymentRecord();
        pay1.setApplicationId(1);
        pay1.setAmount(BigDecimal.valueOf(10.50));
        pay1.setAccountant("acc");
        pay1.setIban("iban");
        pay1.setBic("bic");

        when(repository.getPaymentByApplicationId(1)).thenReturn(pay1);

        when(repository.getPaymentByApplicationId(2)).thenReturn(null);

        when(mockBankClient.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(new PaymentResponseDto(any(), any(), "approved", any()));

        var res = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), 2);

        assertEquals(BallotPeriodService.RunLotteryResultCode.OK, res.code());
        assertEquals(2, res.selected().size());
        assertEquals(1, res.notSelected().size());

        verify(repository).markSelected(periodId, 1);
        verify(repository).markSelected(periodId, 2);

        verify(mockBankClient, times(1)).processPayment(any(PaymentRequestDto.class));
        verify(repository).updatePaymentStatus(1, PaymentStatus.paid);
        verify(repository, never()).updatePaymentStatus(eq(2), any());

        verify(repository).truncateApplicationPayments();
    }

    @Test
    @Disabled
    void runLottery_OK_setsPaymentStatusUnpaid_whenBankNotApproved() {
        int periodId = 100;

        ApplicationRecord a1 = app(10);
        var lottery = new DistributionAlgorithmService.LotteryResult(List.of(a1), List.of());

        when(distributionAlgorithmService.runLotteryForBallotPeriod(eq(periodId), any(), anyInt()))
                .thenReturn(lottery);

        ApplicationPaymentRecord pay = new ApplicationPaymentRecord();
        pay.setApplicationId(10);
        pay.setAmount(BigDecimal.valueOf(1.0));
        pay.setAccountant("acc");
        pay.setIban("iban");
        pay.setBic("bic");

        when(repository.getPaymentByApplicationId(10)).thenReturn(pay);

        when(mockBankClient.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(new PaymentResponseDto(any(), any(), "declined", any()));

        var res = service.runLotteryForBallotPeriod(periodId, anyLicenseType(), 1);

        assertEquals(BallotPeriodService.RunLotteryResultCode.OK, res.code());
        verify(repository).updatePaymentStatus(10, PaymentStatus.unpaid);
        verify(repository).truncateApplicationPayments();
    }

    private static ApplicationRecord app(int id) {
        ApplicationRecord r = new ApplicationRecord();
        r.setId(id);
        return r;
    }

    private static LicenseTypeApiEnum anyLicenseType() {
        return LicenseTypeApiEnum.values()[0];
    }
}