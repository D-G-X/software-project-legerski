package de.hft.licensing.application.restController;

import de.hft.licensing.api.BallotPeriodsApi;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.application.services.authServices.AdminOnly;
import de.hft.licensing.application.services.BallotPeriodService;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
public class BallotPeriodsController implements BallotPeriodsApi {

    private static final Logger log = LicensingLoggerFactory.getLogger(BallotPeriodsController.class);

    private final BallotPeriodService ballotPeriodService;

    public BallotPeriodsController(BallotPeriodService ballotPeriodService) {
        this.ballotPeriodService = ballotPeriodService;
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<BallotPeriodResource>> listBallotPeriods() {
        var rows = ballotPeriodService.listBallotPeriods();

        List<BallotPeriodResource> resources = rows.stream().map(record -> {
            BallotPeriodResource resource = new BallotPeriodResource();
            RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(record, resource);
            resource.setTotalApplications(ballotPeriodService.countApplicationsInPeriod(record.getId()));
            return resource;
        }).toList();

        return ResponseEntity.ok(resources);
    }

    @Override
    @AdminOnly
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

        var res = ballotPeriodService.createBallotPeriod(newStart, newEnd);

        if (res.code() == BallotPeriodService.CreateBallotPeriodResultCode.OVERLAPS) {
            log.warn("Attempted to create overlapping ballot period: {} - {}", newStart, newEnd);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (res.code() == BallotPeriodService.CreateBallotPeriodResultCode.INTERNAL_ERROR || res.record() == null) {
            log.error("Failed to create ballot period: {} - {}", newStart, newEnd);
            return ResponseEntity.status(500).build();
        }

        BallotPeriodResource ballotPeriodResource = new BallotPeriodResource();
        RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(res.record(), ballotPeriodResource);
        return ResponseEntity.ok(ballotPeriodResource);
    }

    @Override
    public ResponseEntity<CurrentBallotPeriodResource> getBallotPeriod() {
        BallotPeriodRecord ballotPeriodRecord = ballotPeriodService.getActiveBallotPeriodUtcNow();
        if (ballotPeriodRecord == null) {
            log.warn("There is no active ballot period at the moment: {}", LocalDateTime.now(ZoneOffset.UTC));
            return ResponseEntity.noContent().build();
        }

        CurrentBallotPeriodResource currentBallotPeriodResource = new CurrentBallotPeriodResource();
        RecordToResourceMapperUtil.mapCurrentBallotPeriodRecordToResource(ballotPeriodRecord, currentBallotPeriodResource);
        return ResponseEntity.ok(currentBallotPeriodResource);
    }

    @Override
    @AdminOnly
    public ResponseEntity<BallotPeriodResource> getBallotPeriodDetails(Integer periodId) {
        if (periodId == null || periodId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BallotPeriodRecord ballotPeriodRecord = ballotPeriodService.getBallotPeriodById(periodId);
        if (ballotPeriodRecord == null) {
            return ResponseEntity.notFound().build();
        }

        BallotPeriodResource ballotPeriodResource = new BallotPeriodResource();
        RecordToResourceMapperUtil.mapBallotPeriodRecordToResource(ballotPeriodRecord, ballotPeriodResource);
        return ResponseEntity.ok(ballotPeriodResource);
    }

    @Override
    @AdminOnly
    public ResponseEntity<List<ApplicationResource>> getBallotPeriodEntries(Integer periodId) {
        List<ApplicationRecord> applicationRecords = ballotPeriodService.getBallotPeriodEntries(periodId);

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
            RunLotteryForBallotPeriodRequest runLotteryForBallotPeriodRequest
    ) {
        if (periodId == null || periodId <= 0) {
            log.warn("Invalid period ID provided for lottery: {}", periodId);
            return ResponseEntity.badRequest().build();
        }

        LicenseTypeApiEnum licenseType = runLotteryForBallotPeriodRequest != null
                ? runLotteryForBallotPeriodRequest.getLicenseType()
                : null;

        var res = ballotPeriodService.runLotteryForBallotPeriod(periodId, licenseType, licensesToDistribute);

        if (res.code() == BallotPeriodService.RunLotteryResultCode.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        if (res.code() == BallotPeriodService.RunLotteryResultCode.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }
        if (res.code() == BallotPeriodService.RunLotteryResultCode.INTERNAL_ERROR || res.selected() == null || res.notSelected() == null) {
            return ResponseEntity.status(500).build();
        }

        List<ApplicationResource> selectedResources = res.selected().stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            return resource;
        }).toList();

        List<ApplicationResource> notSelectedResources = res.notSelected().stream().map(record -> {
            ApplicationResource resource = new ApplicationResource();
            RecordToResourceMapperUtil.mapApplicationRecordToResource(record, resource);
            return resource;
        }).toList();

        RunLotteryForBallotPeriod200Response body = new RunLotteryForBallotPeriod200Response();
        body.setSelectedApplications(selectedResources);
        body.setNotSelectedApplications(notSelectedResources);

        return ResponseEntity.ok(body);
    }
}