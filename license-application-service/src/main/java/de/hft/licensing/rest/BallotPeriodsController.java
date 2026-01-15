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
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.services.DistributionAlgorithmService;
import de.hft.licensing.services.MockBankClient;
import de.hft.licensing.services.PaymentRequestDto;
import de.hft.licensing.services.PaymentResponseDto;
import de.hft.licensing.services.auth.AdminOnly;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import io.swagger.v3.oas.annotations.Parameter;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.jooq.impl.DSL.selectOne;


@RestController
public class BallotPeriodsController implements BallotPeriodsApi {
    private final DefaultDSLContext dslContext;
    private final DistributionAlgorithmService distributionAlgorithmService;
    private final MockBankClient mockBankClient;
    private static final Logger log = LicensingLoggerFactory.getLogger(BallotPeriodsController.class);

    public BallotPeriodsController(DefaultDSLContext dslContext, DistributionAlgorithmService distributionAlgorithmService, MockBankClient mockBankClient) {
        this.dslContext = dslContext;
        this.distributionAlgorithmService = distributionAlgorithmService;
        this.mockBankClient = mockBankClient;
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<BallotPeriodResource>> listBallotPeriods() {
        List<BallotPeriodRecord> ballotPeriodRecords = dslContext.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .orderBy(BallotPeriod.BALLOT_PERIOD.ID.desc())
                .fetchInto(BallotPeriodRecord.class);

        List<BallotPeriodResource> ballotPeriodResources = ballotPeriodRecords.stream().map(record -> {
            BallotPeriodResource resource = new BallotPeriodResource();
            RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(record, resource);
            return resource;
        }).toList();

        // Set amount of applications for each ballot period
        for (BallotPeriodResource resource : ballotPeriodResources) {
            Integer applicationCount = dslContext.fetchCount(
                    dslContext.select()
                            .from(BallotPeriod.BALLOT_PERIOD)
                            .join(Ballot.BALLOT)
                                .on(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(BallotPeriod.BALLOT_PERIOD.ID))
                            .where(BallotPeriod.BALLOT_PERIOD.ID.eq(resource.getBallotPeriodId()))
            );
            resource.setTotalApplications(applicationCount);
        }

        return ResponseEntity.ok(ballotPeriodResources);
    }

    @Override
    @AdminOnly
    @Transactional
    public ResponseEntity<BallotPeriodResource> createBallotPeriod(CreateBallotPeriodRequest createBallotPeriodRequest) {
        if (createBallotPeriodRequest == null
                || createBallotPeriodRequest.getStartDate() == null
                || createBallotPeriodRequest.getEndDate() == null
                || createBallotPeriodRequest.getStartDate().isAfter(createBallotPeriodRequest.getEndDate())) {
            log.warn("Invalid ballot period creation request: {}", createBallotPeriodRequest);
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime newStart = createBallotPeriodRequest.getStartDate()
                .atZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime newEnd = createBallotPeriodRequest.getEndDate()
                .atZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        boolean overlaps = dslContext.fetchExists(
                selectOne()
                        .from(BallotPeriod.BALLOT_PERIOD)
                        .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(newEnd))
                        .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(newStart))
        );

        if (overlaps) {
            log.warn("Attempted to create overlapping ballot period: {} - {}", newStart, newEnd);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        BallotPeriodRecord ballotPeriodRecord = dslContext.insertInto(BallotPeriod.BALLOT_PERIOD)
                .set(BallotPeriod.BALLOT_PERIOD.START_DATE, newStart)
                .set(BallotPeriod.BALLOT_PERIOD.END_DATE, newEnd)
                .returning()
                .fetchOneInto(BallotPeriodRecord.class);

        if (ballotPeriodRecord == null) {
            log.error("Failed to create ballot period: {} - {}", newStart, newEnd);
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
        } else {
            log.info("No ballot periods found in the system.");
            return ResponseEntity.notFound().build();
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
            log.warn("No applications found for Ballot Period ID {}", periodId);
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
            log.warn("Invalid period ID provided for lottery: {}", periodId);
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
            log.error("Error running lottery for Ballot Period ID {}: {}", periodId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Error running lottery for Ballot Period ID {}: {}", periodId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        if (result == null) {
            log.error("Lottery could not be run for Ballot Period ID {}. Check if the period exists and is finished.", periodId);
            return ResponseEntity.badRequest().build();
        }

        List<ApplicationResource> selectedResources = result.selectedApplications().stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            try {
                dslContext.update(Ballot.BALLOT)
                        .set(Ballot.BALLOT.SELECTED, true)
                        .where(Ballot.BALLOT.APPLICATION_ID.eq(record.getId()))
                        .and(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(periodId))
                        .execute();
            } catch (Exception e) {
                log.error("Failed to map application ID {} to resource: {}", record.getId(), e.getMessage());
            }
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

            PaymentStatus paymentStatus = PaymentStatus.unpaid;
            if (resp.status().equals("approved")) {
                paymentStatus = PaymentStatus.paid;
            }

            dslContext.update(ApplicationPayment.APPLICATION_PAYMENT)
                    .set(ApplicationPayment.APPLICATION_PAYMENT.PAYMENT_STATUS, paymentStatus)
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
