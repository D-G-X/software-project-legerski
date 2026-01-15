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

    public void updateApplicationInBallotTableToSelected(int periodId, ApplicationRecord app) {
        dsl.update(Ballot.BALLOT)
                .set(Ballot.BALLOT.SELECTED, true)
                .where(String.valueOf(Ballot.BALLOT.APPLICATION_ID), app.getId())
                .and(String.valueOf(Ballot.BALLOT.BALLOT_PERIOD_ID), periodId)
                .execute();

    }

    public void updateApplicationStatusToApproved(ApplicationRecord app){
        dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_APPROVED)
                .where(Application.APPLICATION.ID.eq(app.getId()))
                .execute();
    }

    public void updateApplicationStatusToRejected(ApplicationRecord app){
        dsl.update(Application.APPLICATION)
                .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_REJECTED)
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

    public BallotPeriodRecord getBallotPeriodById(int periodId){
        return dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOneInto(BallotPeriodRecord.class);
    }

    public void deleteApplications(List<ApplicationRecord> applications) {
        List<Integer> appIds = new ArrayList<>();
        for (ApplicationRecord app : applications) {
            appIds.add(app.getId());
        }


    }

}
