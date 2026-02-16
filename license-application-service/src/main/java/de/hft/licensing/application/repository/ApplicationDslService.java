package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.VerificationStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.Ballot;
import de.hft.licensing.db.tables.BallotPeriod;
import de.hft.licensing.db.tables.User;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationDslService {

    private final DSLContext dsl;

    public ApplicationDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean isBallotPeriodActive(LocalDateTime nowUtc) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(BallotPeriod.BALLOT_PERIOD)
                        .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(nowUtc))
                        .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(nowUtc))
        );
    }

    public Integer getCurrentBallotPeriodId(LocalDateTime nowUtc) {
        return dsl.select(BallotPeriod.BALLOT_PERIOD.ID)
                .from(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(nowUtc))
                .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(nowUtc))
                .fetchOneInto(Integer.class);
    }

    public boolean userExists(UUID userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(User.USER)
                        .where(User.USER.ID.eq(userId.toString()))
        );
    }

    public ApplicationRecord createApplication(
            UUID userId,
            String cadastralReference,
            LicenseType licenseType,
            String remarks,
            LocalDateTime nowUtc
    ) {
        return dsl.insertInto(Application.APPLICATION)
                .set(Application.APPLICATION.USER_ID, userId.toString())
                .set(Application.APPLICATION.APPLICATION_STATUS, ApplicationStatus.draft)
                .set(Application.APPLICATION.APPLIED_AT, nowUtc)
                .set(Application.APPLICATION.CHANGED_AT, nowUtc)
                .set(Application.APPLICATION.CADASTRAL_REFERENCE, cadastralReference)
                .set(Application.APPLICATION.LICENSE_TYPE, licenseType)
                .set(Application.APPLICATION.REMARKS, remarks)
                .set(Application.APPLICATION.VERIFICATION_STATUS, VerificationStatus.pending)
                .returning()
                .fetchOneInto(ApplicationRecord.class);
    }

    public int createBallotEntry(Integer ballotPeriodId, Integer applicationId) {
        return dsl.insertInto(Ballot.BALLOT)
                .set(Ballot.BALLOT.BALLOT_PERIOD_ID, ballotPeriodId)
                .set(Ballot.BALLOT.APPLICATION_ID, applicationId)
                .set(Ballot.BALLOT.SELECTED, false)
                .execute();
    }

    public int deleteApplication(Integer applicationId) {
        return dsl.deleteFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .execute();
    }

    public ApplicationRecord getApplication(Integer applicationId) {
        return dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);
    }

    public List<ApplicationRecord> listApplications(UUID userId, ApplicationStatus status) {
        if (userId == null && status == null) {
            return dsl.selectFrom(Application.APPLICATION)
                    .fetchInto(ApplicationRecord.class);
        } else if (userId != null && status == null) {
            return dsl.selectFrom(Application.APPLICATION)
                    .where(Application.APPLICATION.USER_ID.eq(userId.toString()))
                    .fetchInto(ApplicationRecord.class);
        } else if (userId == null) {
            return dsl.selectFrom(Application.APPLICATION)
                    .where(Application.APPLICATION.APPLICATION_STATUS.eq(status))
                    .fetchInto(ApplicationRecord.class);
        } else {
            return dsl.selectFrom(Application.APPLICATION)
                    .where(Application.APPLICATION.USER_ID.eq(userId.toString())
                            .and(Application.APPLICATION.APPLICATION_STATUS.eq(status)))
                    .fetchInto(ApplicationRecord.class);
        }
    }

    public ApplicationStatus getApplicationStatus(Integer applicationId) {
        return dsl.select(Application.APPLICATION.APPLICATION_STATUS)
                .from(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationStatus.class);
    }

    public String getApplicationRemarks(Integer applicationId) {
        return dsl.select(Application.APPLICATION.REMARKS)
                .from(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(String.class);
    }

    public ApplicationRecord updateApplication(Integer applicationId, Map<Field<?>, Object> updates) {
        return dsl.update(Application.APPLICATION)
                .set(updates)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .returning()
                .fetchOneInto(ApplicationRecord.class);
    }
}