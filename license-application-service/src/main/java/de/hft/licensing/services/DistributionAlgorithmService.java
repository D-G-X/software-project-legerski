package de.hft.licensing.services;

import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.model.LicenseTypeApiEnum;
import de.hft.licensing.services.dslService.BallotDslService;
import de.hft.licensing.utils.EnumMapperUtil;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DistributionAlgorithmService {

    private static final int DEFAULT_MAX_ACCEPTED_APPLICATIONS = 100;

    private final BallotDslService dslService;

    public DistributionAlgorithmService(BallotDslService dslService) {
        this.dslService = dslService;
    }


    @Transactional
    public LotteryResult runLotteryForBallotPeriod(int periodId,
                                                   @Nullable LicenseTypeApiEnum licenseType,
                                                   @Nullable Integer maxAcceptedOverride) {

        BallotPeriodRecord period = dslService.getBallotPeriodById(periodId);

        if (period == null) {
            return null;
        }

        //period not over yet
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (now.isBefore(period.getEndDate())) {
            return null;
        }

        LocalDateTime endDate = period.getEndDate();
        LocalDateTime startDate = period.getStartDate();

        List<ApplicationRecord> candidates = dslService.getCandidateApplications(startDate, endDate, licenseType);

        if (candidates.isEmpty()) {
            return new LotteryResult(Collections.emptyList(), Collections.emptyList());
        }

        Collections.shuffle(new ArrayList<>(candidates));

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

                String userId = applicationRecord.getUserId();
                long alreadyForUser = selected.stream()
                        .filter(a -> a.getUserId().equals(userId))
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
            dslService.updateApplicationInBallotTableToSelected(periodId, app);
            createLicenseForApplication(app);
            dslService.updateApplicationStatusToApproved(app);
        }

        for (ApplicationRecord app : notSelected) {
            dslService.updateApplicationStatusToRejected(app);
        }

        return new LotteryResult(selected, notSelected);
    }


    private void createLicenseForApplication(ApplicationRecord applicationRecord) {
        LicenseTypeApiEnum licenseType = EnumMapperUtil.getPendantFromEnum(applicationRecord.getLicenseType());
        dslService.createLicenseForApplication(applicationRecord, licenseType);
    }

    public record LotteryResult(List<ApplicationRecord> selectedApplications,
                                List<ApplicationRecord> notSelectedApplications) {
    }
}