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
import org.jooq.impl.DefaultDSLContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DistributionAlgorithmService {

    private static final int DEFAULT_MAX_ACCEPTED_APPLICATIONS = 20_000;

    private static final ApplicationStatus STATUS_SUBMITTED = ApplicationStatus.submitted;
    private static final ApplicationStatus STATUS_APPROVED  = ApplicationStatus.approved;

    private final DefaultDSLContext dsl;

    public DistributionAlgorithmService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }


    @Transactional
    public LotteryResult runLotteryForBallotPeriod(int periodId,
                                                   @Nullable LicenseTypeApiEnum licenseType,
                                                   @Nullable Integer maxAcceptedOverride) {

        BallotPeriodRecord period = dsl.selectFrom(BallotPeriod.BALLOT_PERIOD)
                .where(BallotPeriod.BALLOT_PERIOD.ID.eq(periodId))
                .fetchOne();

        // no period found
        if (period == null) {
            return null;
        }

        //period not over yet
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (now.isBefore(period.getEndDate())) {
            return null;
        }

        LocalDateTime endDate = period.getEndDate();

        // all applications with status SUBMITTED and applied before period end date
        List<ApplicationRecord> candidates = dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.APPLICATION_STATUS.eq(STATUS_SUBMITTED)
                        .and(Application.APPLICATION.APPLIED_AT.le(endDate)))
                .fetchInto(ApplicationRecord.class);

        if (candidates.isEmpty()) {
            return new LotteryResult(Collections.emptyList(), Collections.emptyList());
        }

        // sort by appliedAt, then id
        candidates.sort(
                Comparator.comparing(ApplicationRecord::getAppliedAt)
                        .thenComparing(ApplicationRecord::getId)
        );

        int maxAccepted = (maxAcceptedOverride != null && maxAcceptedOverride > 0)
                ? maxAcceptedOverride
                : DEFAULT_MAX_ACCEPTED_APPLICATIONS;

        // fair algorithm:
        // 1st Round: max. 1 Application per User
        // 2nd Round: max. 2 Applications per User
        // until maxAccepted resched or no cands left
        List<ApplicationRecord> selected = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();

        int perUserQuota = 1;
        while (selected.size() < maxAccepted && selected.size() < candidates.size()) {
            boolean pickedInThisRound = false;

            for (ApplicationRecord applicationRecord : candidates) {
                if (selected.size() >= maxAccepted) {
                    break;
                }
                if (selectedIds.contains(applicationRecord.getId())) {
                    continue;
                }

                Integer userId = Integer.valueOf(applicationRecord.getUserId());
                long alreadyForUser = selected.stream()
                        .filter(a -> Objects.equals(Integer.valueOf(a.getUserId()), userId))
                        .count();

                if (alreadyForUser >= perUserQuota) {
                    continue;
                }

                selected.add(applicationRecord);
                selectedIds.add(applicationRecord.getId());
                pickedInThisRound = true;
            }

            if (!pickedInThisRound) {
                break;
            }
            perUserQuota++;
        }

        List<ApplicationRecord> notSelected = candidates.stream()
                .filter(a -> !selectedIds.contains(a.getId()))
                .collect(Collectors.toList());


        for (ApplicationRecord app : selected) {
            dsl.insertInto(Ballot.BALLOT)
                    .set(Ballot.BALLOT.BALLOT_PERIOD_ID, periodId)
                    .set(Ballot.BALLOT.APPLICATION_ID, app.getId())
                    .set(Ballot.BALLOT.SELECTED, true)
                    .execute();

            createLicenseForApplication(app, licenseType);

            dsl.update(Application.APPLICATION)
                    .set(Application.APPLICATION.APPLICATION_STATUS, STATUS_APPROVED)
                    .where(Application.APPLICATION.ID.eq(app.getId()))
                    .execute();
        }

        return new LotteryResult(selected, notSelected);
    }


    private void createLicenseForApplication(ApplicationRecord applicationRecord,
                                             @Nullable LicenseTypeApiEnum licenseType) {

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

    public record LotteryResult(List<ApplicationRecord> selectedApplications,
                                List<ApplicationRecord> notSelectedApplications) {
    }
}