package de.hft.licensing.services.dslService;

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
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.Condition;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class BallotDslService {

    private static final ApplicationStatus STATUS_PAYMENT_RECEIVED = ApplicationStatus.payment_received;
    private static final ApplicationStatus STATUS_APPROVED  = ApplicationStatus.approved;
    private static final ApplicationStatus STATUS_REJECTED  = ApplicationStatus.rejected;

    public BallotDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    private final DefaultDSLContext dsl;

    /**
     * Updates the ballot table to mark the given application as selected for the specified ballot period.
     *
     * @param periodId The ID of the ballot period.
     * @param app      The application record to be marked as selected.
     */
    public void updateApplicationInBallotTableToSelected(int periodId, ApplicationRecord app) {
        dsl.update(Ballot.BALLOT)
                .set(Ballot.BALLOT.SELECTED, true)
                .where(Ballot.BALLOT.APPLICATION_ID.eq(app.getId()))
                .and(Ballot.BALLOT.BALLOT_PERIOD_ID.eq(periodId))
                .execute();

    }

    /**
     * Updates the status of the given application to "approved".
     *
     * @param app The application record to be updated.
     */
    public void updateApplicationStatusToApproved(ApplicationRecord app){
        dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_APPROVED)
                .where(Application.APPLICATION.ID.eq(app.getId()))
                .execute();
    }

    /**
     * Updates the status of the given application to "rejected".
     *
     * @param app The application record to be updated.
     */
    public void updateApplicationStatusToRejected(ApplicationRecord app){
        dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_REJECTED)
                .where(Application.APPLICATION.ID.eq(app.getId()))
                .execute();
    }

    /**
     * Creates a license for the given application.
     *
     * @param applicationRecord The application record for which the license is to be created.
     * @param licenseType       The type of license to be created.
     * @return The number of affected rows (should be 1 if successful).
     */
    public int createLicenseForApplication(ApplicationRecord applicationRecord, LicenseTypeApiEnum licenseType){
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        return dsl.insertInto(License.LICENSE)
                .set(License.LICENSE.USER_ID, applicationRecord.getUserId())
                .set(License.LICENSE.APPLICATION_ID, applicationRecord.getId())
                .set(License.LICENSE.LICENSE_TYPE, (LicenseType) EnumMapperUtil.getPendantFromEnum(licenseType))
                .set(License.LICENSE.LICENSE_STATUS, LicenseStatus.active)
                .set(License.LICENSE.ISSUED_AT, now)
                .set(License.LICENSE.EXPIRES_AT, now.plusYears(5))
                .execute();
    }

    /**
     * Retrieves candidate applications based on the specified criteria.
     *
     * @param startDate   The start date of the application period.
     * @param endDate     The end date of the application period.
     * @param licenseType The type of license to filter by (can be null).
     * @return A list of candidate application records.
     */
    public List<ApplicationRecord> getCandidateApplications (LocalDateTime startDate, LocalDateTime endDate, LicenseTypeApiEnum licenseType){
        // all applications with status payment_received and applied in period, if type != null filter by type
        Condition condition = Application.APPLICATION.APPLICATION_STATUS.eq(STATUS_PAYMENT_RECEIVED)
                .and(Application.APPLICATION.APPLIED_AT.le(endDate))
                .and(Application.APPLICATION.APPLIED_AT.ge(startDate));
        if (licenseType != null) {
            condition = condition.and(Application.APPLICATION.LICENSE_TYPE.eq((LicenseType) EnumMapperUtil.getPendantFromEnum(licenseType)));
        }

        return new ArrayList<>(
                dsl.selectFrom(Application.APPLICATION)
                        .where(condition)
                        .fetchInto(ApplicationRecord.class)
        );
    }

    /**
     * Retrieves a ballot period by its ID.
     *
     * @param periodId The ID of the ballot period.
     * @return The BallotPeriodRecord corresponding to the given ID.
     */
    public BallotPeriodRecord getBallotPeriodById(int periodId){
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOneInto(BallotPeriodRecord.class);
    }

    /**
     * Deletes applications from the database. Should be used with caution.
     *
     * @param applications List of ApplicationRecord objects to be deleted.
     */
    public void deleteApplications(List<ApplicationRecord> applications) {
        List<Integer> appIds = new ArrayList<>();
        for (ApplicationRecord app : applications) {
            appIds.add(app.getId());
        }
    }

    public int getCurrentBallotPeriodId() {
        var ballotPeriodRecord = dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.START_DATE.le(LocalDateTime.now(Clock.systemUTC())))
                .and(BallotPeriod.BALLOT_PERIOD.END_DATE.ge(LocalDateTime.now(Clock.systemUTC())))
                .fetchOne();
        if(ballotPeriodRecord == null){
            return 0;
        }
        return ballotPeriodRecord.getId();
    }

}
