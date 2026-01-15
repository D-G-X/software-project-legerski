package de.hft.licensing.services;

import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.BallotPeriodRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.LicenseTypeApiEnum;
import de.hft.licensing.services.dslService.BallotDslService;
import de.hft.licensing.utils.EnumMapperUtil;
import org.slf4j.Logger;
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
    private final Logger log = LicensingLoggerFactory.getLogger(DistributionAlgorithmService.class);

    public DistributionAlgorithmService(BallotDslService dslService) {
        this.dslService = dslService;
    }


    /**
     * Runs the lottery for a given ballot period.
     * A brief explanation of the algorithm can be found in the readme.md file.
     *
     * @param periodId            The ID of the ballot period.
     * @param licenseType         Optional license type to filter applications. (nullable)
     * @param maxAcceptedOverride Optional maximum number of accepted applications. (nullable)
     * @return LotteryResult containing selected and not selected applications, or null if period is invalid or not over yet.
     */
    @Transactional
    public LotteryResult runLotteryForBallotPeriod(int periodId,
                                                   @Nullable LicenseTypeApiEnum licenseType,
                                                   @Nullable Integer maxAcceptedOverride) {

        BallotPeriodRecord period = dslService.getBallotPeriodById(periodId);

        if (period == null) {
            log.error("Lottery run failed: Ballot period with ID {} does not exist.", periodId);
            return null;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (now.isBefore(period.getEndDate())) {
            log.error("Lottery run failed: Ballot period with ID {} is not over yet.", periodId);
            return null;
        }

        LocalDateTime endDate = period.getEndDate();
        LocalDateTime startDate = period.getStartDate();

        List<ApplicationRecord> candidates = dslService.getCandidateApplications(startDate, endDate, licenseType);

        if (candidates.isEmpty()) {
            log.warn("No candidate applications found for ballot period ID {}.", periodId);
            return new LotteryResult(Collections.emptyList(), Collections.emptyList());
        }

        Random seed = new Random(System.currentTimeMillis());
        Collections.shuffle(new ArrayList<>(candidates), seed);
        log.info("Shuffled {} candidate applications for ballot period ID {} with seed {}", candidates.size(), periodId, seed);

        int maxAccepted = (maxAcceptedOverride != null && maxAcceptedOverride > 0)
                ? maxAcceptedOverride
                : DEFAULT_MAX_ACCEPTED_APPLICATIONS;

        if (maxAccepted > DEFAULT_MAX_ACCEPTED_APPLICATIONS) {
            log.warn("Max accepted applications overridden to {} for ballot period ID {}.", maxAccepted, periodId);
        }

        List<ApplicationRecord> selected = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();
        int perUserQuota = 1;

        /*
         * The main selection loop that iteratively selects applications based on the per-user quota.
         * It continues until the maximum number of accepted applications is reached or there are no more candidates.
         * This way, no User can dominate the selection, as in each round only one application per user is accepted.
         */
        log.info("Starting selection process for ballot period ID {}: maxAccepted={}, totalCandidates={}",
                periodId, maxAccepted, candidates.size());
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
        log.info("Selection process completed for ballot period ID {}: selected={}, notSelected={}",
                periodId, selected.size(), candidates.size() - selected.size());
        log.info("Postprocessing selected applications for ballot period ID {}.", periodId);

        List<ApplicationRecord> notSelected = candidates.stream()
                .filter(a -> !selectedIds.contains(a.getId()))
                .collect(Collectors.toList());

        int licenseCreationCount = 0;
        for (ApplicationRecord app : selected) {
            dslService.updateApplicationInBallotTableToSelected(periodId, app);
            licenseCreationCount += createLicenseForApplication(app);
            dslService.updateApplicationStatusToApproved(app);
        }

        for (ApplicationRecord app : notSelected) {
            dslService.updateApplicationStatusToRejected(app);
        }
        log.info("Postprocessing completed for ballot period ID {}.", periodId);
        log.info("Lottery run completed for ballot period ID {}. Selected applications: {}, Not selected applications: {}, Created licenses: {}",
                periodId, selected.size(), notSelected.size(), licenseCreationCount);
        return new LotteryResult(selected, notSelected);
    }


    /**
     * Creates a license for the given application record.
     *
     * @param applicationRecord The application record for which to create a license.
     */
    private int createLicenseForApplication(ApplicationRecord applicationRecord) {
        LicenseTypeApiEnum licenseType = EnumMapperUtil.getPendantFromEnum(applicationRecord.getLicenseType());
        return dslService.createLicenseForApplication(applicationRecord, licenseType);
    }

    /**
     * Record to hold the results of the lottery.
     *
     * @param selectedApplications    List of applications that were selected.
     * @param notSelectedApplications List of applications that were not selected.
     */
    public record LotteryResult(List<ApplicationRecord> selectedApplications,
                                List<ApplicationRecord> notSelectedApplications) {
    }
}