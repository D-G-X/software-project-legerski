package de.hft.licensing.application.services;

import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.LicenseTypeApiEnum;
import de.hft.licensing.application.dto.PaymentRequestDto;
import de.hft.licensing.application.dto.PaymentResponseDto;
import de.hft.licensing.application.repository.BallotPeriodDslService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class BallotPeriodService {

    private final BallotPeriodDslService repository;
    private final DistributionAlgorithmService distributionAlgorithmService;
    private final MockBankService mockBankClient;

    public BallotPeriodService(
            BallotPeriodDslService repository,
            DistributionAlgorithmService distributionAlgorithmService,
            MockBankService mockBankClient
    ) {
        this.repository = repository;
        this.distributionAlgorithmService = distributionAlgorithmService;
        this.mockBankClient = mockBankClient;
    }

    public enum CreateBallotPeriodResultCode {
        OK,
        OVERLAPS,
        INTERNAL_ERROR
    }

    public record CreateBallotPeriodResult(CreateBallotPeriodResultCode code, BallotPeriodRecord record) {}

    public enum RunLotteryResultCode {
        OK,
        NOT_FOUND,
        BAD_REQUEST,
        INTERNAL_ERROR
    }

    public record RunLotteryResult(RunLotteryResultCode code,
                                   List<ApplicationRecord> selected,
                                   List<ApplicationRecord> notSelected) {}

    public List<BallotPeriodRecord> listBallotPeriods() {
        return repository.listBallotPeriods();
    }

    public int countApplicationsInPeriod(Integer periodId) {
        return repository.countApplicationsInPeriod(periodId);
    }

    @Transactional
    public CreateBallotPeriodResult createBallotPeriod(LocalDateTime startUtc, LocalDateTime endUtc) {
        boolean overlaps = repository.overlaps(startUtc, endUtc);
        if (overlaps) {
            return new CreateBallotPeriodResult(CreateBallotPeriodResultCode.OVERLAPS, null);
        }

        BallotPeriodRecord rec = repository.createBallotPeriod(startUtc, endUtc);
        if (rec == null) {
            return new CreateBallotPeriodResult(CreateBallotPeriodResultCode.INTERNAL_ERROR, null);
        }

        return new CreateBallotPeriodResult(CreateBallotPeriodResultCode.OK, rec);
    }

    public BallotPeriodRecord getActiveBallotPeriodUtcNow() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        return repository.getActiveBallotPeriod(nowUtc);
    }

    public BallotPeriodRecord getBallotPeriodById(Integer periodId) {
        return repository.getBallotPeriodById(periodId);
    }

    public List<ApplicationRecord> getBallotPeriodEntries(Integer periodId) {
        return repository.getBallotPeriodEntries(periodId);
    }

    @Transactional
    public RunLotteryResult runLotteryForBallotPeriod(Integer periodId, LicenseTypeApiEnum licenseType, Integer licensesToDistribute) {
        DistributionAlgorithmService.LotteryResult lottery;
        try {
            lottery = distributionAlgorithmService.runLotteryForBallotPeriod(periodId, licenseType, licensesToDistribute);
        } catch (IllegalArgumentException e) {
            return new RunLotteryResult(RunLotteryResultCode.NOT_FOUND, null, null);
        } catch (IllegalStateException e) {
            return new RunLotteryResult(RunLotteryResultCode.BAD_REQUEST, null, null);
        } catch (Exception e) {
            return new RunLotteryResult(RunLotteryResultCode.INTERNAL_ERROR, null, null);
        }

        if (lottery == null) {
            return new RunLotteryResult(RunLotteryResultCode.BAD_REQUEST, null, null);
        }

        for (ApplicationRecord app : lottery.selectedApplications()) {
            repository.markSelected(periodId, app.getId());

            ApplicationPaymentRecord pay = repository.getPaymentByApplicationId(app.getId());
            if (pay == null) {
                continue;
            }

            PaymentRequestDto req = new PaymentRequestDto(
                    String.valueOf(app.getId()),
                    pay.getAmount().doubleValue(),
                    pay.getAccountant(),
                    pay.getIban(),
                    pay.getBic()
            );

            PaymentResponseDto resp = mockBankClient.processPayment(req);

            PaymentStatus paymentStatus = PaymentStatus.unpaid;
            if ("approved".equals(resp.status())) {
                paymentStatus = PaymentStatus.paid;
            }

            repository.updatePaymentStatus(app.getId(), paymentStatus);
        }

        repository.truncateApplicationPayments();

        return new RunLotteryResult(
                RunLotteryResultCode.OK,
                lottery.selectedApplications(),
                lottery.notSelectedApplications()
        );
    }
}