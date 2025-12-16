package de.hft.licensing.services;

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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class BallotDslService {

    private static final ApplicationStatus STATUS_SUBMITTED = ApplicationStatus.submitted;
    private static final ApplicationStatus STATUS_APPROVED  = ApplicationStatus.approved;

    public BallotDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    private final DefaultDSLContext dsl;

    public void insertApplicationInBallotTable(int periodId, ApplicationRecord app) {
        dsl.insertInto(Ballot.BALLOT)
                .set(Ballot.BALLOT.BALLOT_PERIOD_ID, periodId)
                .set(Ballot.BALLOT.APPLICATION_ID, app.getId())
                .set(Ballot.BALLOT.SELECTED, true)
                .execute();

    }

    public void updateApplicationStatusToApproved(ApplicationRecord app){
        dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_APPROVED)
                .where(Application.APPLICATION.ID.eq(app.getId()))
                .execute();
    }

    public void createLicenseForApplication(ApplicationRecord applicationRecord, LicenseTypeApiEnum licenseType){
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        dsl.insertInto(License.LICENSE)
                .set(License.LICENSE.USER_ID, applicationRecord.getUserId())
                .set(License.LICENSE.APPLICATION_ID, applicationRecord.getId())
                .set(License.LICENSE.LICENSE_TYPE, (LicenseType) EnumMapperUtil.getPendantFromEnum(licenseType))
                .set(License.LICENSE.LICENSE_STATUS, LicenseStatus.active)
                .set(License.LICENSE.ISSUED_AT, now)
                .set(License.LICENSE.EXPIRES_AT, now.plusYears(5))
                .execute();
    }

    public List<ApplicationRecord> getCandidateApplications (LocalDateTime startDate, LocalDateTime endDate, LicenseTypeApiEnum licenseType){
        // all applications with status SUBMITTED and applied in period, if type != null filter by type
        Condition condition = Application.APPLICATION.APPLICATION_STATUS.eq(STATUS_SUBMITTED)
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

    public BallotPeriodRecord getBallotPeriodById(int periodId){
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOneInto(BallotPeriodRecord.class);
    }

}
