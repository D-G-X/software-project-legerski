package de.hft.licensing.rest;

import de.hft.licensing.api.BallotPeriodsApi;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.ApplicationPayment;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.*;
import de.hft.licensing.services.DistributionAlgorithmService;
import de.hft.licensing.services.MockBankClient;
import de.hft.licensing.services.PaymentRequestDto;
import de.hft.licensing.services.PaymentResponseDto;
import de.hft.licensing.services.auth.AdminOnly;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import io.swagger.v3.oas.annotations.Parameter;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.jooq.impl.DSL.selectOne;


@RestController
public class BallotPeriodsController implements BallotPeriodsApi {
    private final DefaultDSLContext dslContext;
    private final DistributionAlgorithmService distributionAlgorithmService;
    private final MockBankClient mockBankClient;

    public BallotPeriodsController(DefaultDSLContext dslContext, DistributionAlgorithmService distributionAlgorithmService, MockBankClient mockBankClient) {
        this.dslContext = dslContext;
        this.distributionAlgorithmService = distributionAlgorithmService;
        this.mockBankClient = mockBankClient;
    }

    public record BallotApiError(String code, String message) {}

    @Override
    @AdminOnly
    @Transactional
    public ResponseEntity<BallotPeriodResource> createBallotPeriod(CreateBallotPeriodRequest createBallotPeriodRequest) {
        if (createBallotPeriodRequest == null
                || createBallotPeriodRequest.getStartDate() == null
                || createBallotPeriodRequest.getEndDate() == null
                || createBallotPeriodRequest.getStartDate().isAfter(createBallotPeriodRequest.getEndDate())) {
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime newStart = createBallotPeriodRequest.getStartDate()
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime newEnd = createBallotPeriodRequest.getEndDate()
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        boolean overlaps = dslContext.fetchExists(
                selectOne()
                        .from(BallotPeriod.BALLOT_PERIOD)
                        .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(newEnd))
                        .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(newStart))
        );

        if (overlaps) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        BallotPeriodRecord ballotPeriodRecord = dslContext.insertInto(BallotPeriod.BALLOT_PERIOD)
                .set(BallotPeriod.BALLOT_PERIOD.START_DATE, newStart)
                .set(BallotPeriod.BALLOT_PERIOD.END_DATE, newEnd)
                .returning()
                .fetchOneInto(BallotPeriodRecord.class);

        if (ballotPeriodRecord == null) {
            return ResponseEntity.status(500).build();
        }

        BallotPeriodResource ballotPeriodResource = new BallotPeriodResource();
        RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(ballotPeriodRecord, ballotPeriodResource);
        return ResponseEntity.ok(ballotPeriodResource);
    }

    @Override
    public ResponseEntity<CurrentBallotPeriodResource> getBallotPeriod() {
        BallotPeriodRecord ballotPeriodRecord = dslContext
                .selectFrom(BallotPeriod.BALLOT_PERIOD)
                .orderBy(BallotPeriod.BALLOT_PERIOD.ID.desc())
                .limit(1)
                .fetchOneInto(BallotPeriodRecord.class);

        CurrentBallotPeriodResource currentBallotPeriodResource = new CurrentBallotPeriodResource();

        if(ballotPeriodRecord != null){
            RecordToResourceMapperUtil.mapCurrentBallotPeriodRecordToResource(ballotPeriodRecord, currentBallotPeriodResource);
        }

        return ResponseEntity.ok(currentBallotPeriodResource);
    }

    @Override
    @AdminOnly
    public ResponseEntity<BallotPeriodResource> getBallotPeriodDetails(Integer periodId) {
        if (periodId == null || periodId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BallotPeriodRecord ballotPeriodRecord = dslContext.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOneInto(BallotPeriodRecord.class);

        BallotPeriodResource ballotPeriodResource = new BallotPeriodResource();
        RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(ballotPeriodRecord, ballotPeriodResource);

        return ballotPeriodRecord != null ? ResponseEntity.ok(ballotPeriodResource) : ResponseEntity.notFound().build();
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<ApplicationResource>> getBallotPeriodEntries(Integer periodId) {

        List<ApplicationRecord> applicationRecords= dslContext.select()
                .from(BallotPeriod.BALLOT_PERIOD)
                .join(Ballot.BALLOT)
                    .on(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(BallotPeriod.BALLOT_PERIOD.ID))
                .join(Application.APPLICATION)
                    .on(Ballot.BALLOT.APPLICATION_ID.eq(Application.APPLICATION.ID))
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .and(Application.APPLICATION.APPLICATION_STATUS.eq(ApplicationStatus.submitted))
                .fetchInto(ApplicationRecord.class);

        if (applicationRecords.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ApplicationResource> applicationResources = applicationRecords.stream().map(applicationRecord -> {
            ApplicationResource applicationResource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(applicationRecord, applicationResource);
            return applicationResource;
        }).toList();

        return ResponseEntity.ok(applicationResources);
    }

    @Override
    @AdminOnly
    public ResponseEntity<RunLotteryForBallotPeriod200Response> runLotteryForBallotPeriod(
            Integer periodId,
            @Parameter(name = "licenses_to_distribute") Integer licensesToDistribute,
            RunLotteryForBallotPeriodRequest runLotteryForBallotPeriodRequest) {

        if (periodId == null || periodId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        LicenseTypeApiEnum licenseType = runLotteryForBallotPeriodRequest != null
                ? runLotteryForBallotPeriodRequest.getLicenseType()
                : null;

        DistributionAlgorithmService.LotteryResult result;
        try {
            result = distributionAlgorithmService.runLotteryForBallotPeriod(
                    periodId,
                    licenseType,
                    licensesToDistribute
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }

        if (result == null) {
            System.out.println("[ERROR] - Lottery could not be run for Ballot Period ID " + periodId + ". Check if the period exists and is finished.");
            return ResponseEntity.badRequest().build();
        }

        List<ApplicationResource> selectedResources = result.selectedApplications().stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            return resource;
        }).toList();

        List<ApplicationResource> notSelectedResources = result.notSelectedApplications().stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            return resource;
        }).toList();

        // process payments for selected applications
        for (ApplicationRecord app : result.selectedApplications()) {
            Integer appId = app.getId();

            ApplicationPaymentRecord pay = dslContext.selectFrom(ApplicationPayment.APPLICATION_PAYMENT)
                    .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(appId))
                    .fetchOneInto(ApplicationPaymentRecord.class);

            if (pay == null) {
                continue;
            }

            PaymentRequestDto req = new PaymentRequestDto(
                    String.valueOf(appId),
                    pay.getAmount().doubleValue(),
                    pay.getAccountant(),
                    pay.getIban(),
                    pay.getBic()
            );

            PaymentResponseDto resp = mockBankClient.processPayment(req);

            dslContext.update(ApplicationPayment.APPLICATION_PAYMENT)
                    .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, PaymentStatus.lookupLiteral(resp.status()))
                    .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_DATE, LocalDateTime.now())
                    .where(ApplicationPayment.APPLICATION_PAYMENT.APPLICATION_ID.eq(appId))
                    .execute();
        }

        RunLotteryForBallotPeriod200Response body = new RunLotteryForBallotPeriod200Response();
        body.setSelectedApplications(selectedResources);
        body.setNotSelectedApplications(notSelectedResources);

        dslContext.truncate(ApplicationPayment.APPLICATION_PAYMENT).execute();

        return ResponseEntity.ok(body);
    }

}
