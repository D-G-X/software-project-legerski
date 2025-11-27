package de.hft.licensing.rest;

import de.hft.licensing.api.BallotPeriodsApi;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.*;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;

@RestController
public class BallotPeriodsController implements BallotPeriodsApi {
    private final DefaultDSLContext dslContext;

    public BallotPeriodsController(DefaultDSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ResponseEntity<BallotPeriodResource> createBallotPeriod(CreateBallotPeriodRequest createBallotPeriodRequest) {
        if(createBallotPeriodRequest == null || createBallotPeriodRequest.getStartDate() == null || createBallotPeriodRequest.getEndDate() == null
                || createBallotPeriodRequest.getStartDate().isAfter(createBallotPeriodRequest.getEndDate())) {
            return ResponseEntity.badRequest().build();
        }
        BallotPeriodRecord ballotPeriodRecord = dslContext.insertInto(BallotPeriod.BALLOT_PERIOD)
                .set(BallotPeriod.BALLOT_PERIOD.START_DATE, createBallotPeriodRequest.getStartDate().atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime())
                .set(BallotPeriod.BALLOT_PERIOD.END_DATE, createBallotPeriodRequest.getEndDate().atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime())
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
    public ResponseEntity<List<ApplicationResource>> getBallotPeriodEntries(Integer periodId) {

        List<ApplicationRecord> applicationRecords= dslContext.select()
                .from(BallotPeriod.BALLOT_PERIOD)
                .join(Ballot.BALLOT)
                    .on(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(BallotPeriod.BALLOT_PERIOD.ID))
                .join(Application.APPLICATION)
                    .on(Ballot.BALLOT.APPLICATION_ID.eq(Application.APPLICATION.ID))
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
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
    public ResponseEntity<RunLotteryForBallotPeriod200Response> runLotteryForBallotPeriod(Integer periodId, RunLotteryForBallotPeriodRequest runLotteryForBallotPeriodRequest) {
        // implement lottery logic
        // create ballot
        // check if active period
        // select applications in period


        return null;
    }
}
