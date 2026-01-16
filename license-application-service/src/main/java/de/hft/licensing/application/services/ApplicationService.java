package de.hft.licensing.application.services;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.ApplicationStatusApiEnum;
import de.hft.licensing.model.ApplicationUpdate;
import de.hft.licensing.model.LicenseTypeApiEnum;
import de.hft.licensing.application.repository.ApplicationDslService;
import de.hft.licensing.utils.EnumMapperUtil;
import org.jooq.Field;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {

    private static final Logger log = LicensingLoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationDslService repository;

    public ApplicationService(ApplicationDslService repository) {
        this.repository = repository;
    }

    public enum CreateApplicationResultCode {
        OK,
        BALLOT_PERIOD_INACTIVE,
        USER_NOT_FOUND,
        INTERNAL_ERROR
    }

    public record CreateApplicationResult(CreateApplicationResultCode code, ApplicationRecord record) {}

    public enum UpdateApplicationResultCode {
        OK,
        NOT_FOUND,
        INTERNAL_ERROR
    }

    public record UpdateApplicationResult(UpdateApplicationResultCode code, ApplicationRecord record) {}

    @Transactional
    public CreateApplicationResult createApplication(UUID userId,
                                                     LicenseTypeApiEnum licenseTypeApi,
                                                     String cadastralReference,
                                                     String remarks) {
        LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());

        if (!repository.isBallotPeriodActive(nowUtc)) {
            return new CreateApplicationResult(CreateApplicationResultCode.BALLOT_PERIOD_INACTIVE, null);
        }

        if (!repository.userExists(userId)) {
            return new CreateApplicationResult(CreateApplicationResultCode.USER_NOT_FOUND, null);
        }

        LicenseType licenseType = (LicenseType) EnumMapperUtil.getPendantFromEnum(licenseTypeApi);

        ApplicationRecord dbRecord = repository.createApplication(
                userId,
                cadastralReference,
                licenseType,
                remarks,
                nowUtc
        );

        if (dbRecord == null) {
            return new CreateApplicationResult(CreateApplicationResultCode.INTERNAL_ERROR, null);
        }

        Integer currentBallotPeriodId = repository.getCurrentBallotPeriodId(nowUtc);
        if (currentBallotPeriodId == null) {
            return new CreateApplicationResult(CreateApplicationResultCode.INTERNAL_ERROR, null);
        }

        int inserted = repository.createBallotEntry(currentBallotPeriodId, dbRecord.getId());
        if (inserted <= 0) {
            return new CreateApplicationResult(CreateApplicationResultCode.INTERNAL_ERROR, null);
        }

        return new CreateApplicationResult(CreateApplicationResultCode.OK, dbRecord);
    }

    @Transactional
    public boolean deleteApplication(Integer applicationId) {
        return repository.deleteApplication(applicationId) > 0;
    }

    @Transactional(readOnly = true)
    public ApplicationRecord getApplication(Integer applicationId) {
        return repository.getApplication(applicationId);
    }

    @Transactional(readOnly = true)
    public List<ApplicationRecord> listApplications(UUID userId, ApplicationStatusApiEnum applicationStatus) {
        ApplicationStatus status = applicationStatus != null
                ? (ApplicationStatus) EnumMapperUtil.getPendantFromEnum(applicationStatus)
                : null;

        return repository.listApplications(userId, status);
    }

    @Transactional
    public UpdateApplicationResult updateApplication(Integer applicationId, ApplicationUpdate applicationUpdate) {
        LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());

        ApplicationStatus oldStatus = repository.getApplicationStatus(applicationId);
        String oldRemarks = repository.getApplicationRemarks(applicationId);

        Map<Field<?>, Object> updates = new HashMap<>();

        if (applicationUpdate.getApplicationStatus() != null) {
            updates.put(
                    Application.APPLICATION.APPLICATION_STATUS,
                    EnumMapperUtil.getPendantFromEnum(applicationUpdate.getApplicationStatus())
            );
        }
        if (applicationUpdate.getRemarks() != null) {
            updates.put(Application.APPLICATION.REMARKS, applicationUpdate.getRemarks());
        }
        if (applicationUpdate.getCadastralReference() != null) {
            updates.put(Application.APPLICATION.CADASTRAL_REFERENCE, applicationUpdate.getCadastralReference());
        }
        if (applicationUpdate.getLicenseType() != null) {
            updates.put(
                    Application.APPLICATION.LICENSE_TYPE,
                    EnumMapperUtil.getPendantFromEnum(applicationUpdate.getLicenseType())
            );
        }
        updates.put(Application.APPLICATION.CHANGED_AT, nowUtc);

        ApplicationRecord updated = repository.updateApplication(applicationId, updates);
        if (updated == null) {
            return new UpdateApplicationResult(UpdateApplicationResultCode.NOT_FOUND, null);
        }

        log.info(buildUpdateLog(updated, oldStatus, oldRemarks));
        return new UpdateApplicationResult(UpdateApplicationResultCode.OK, updated);
    }

    private static String buildUpdateLog(ApplicationRecord updated, ApplicationStatus oldStatus, String oldRemarks) {
        String logs = String.format("Updated application with ID %d:", updated.getId());
        if (oldStatus != updated.getApplicationStatus()) {
            String oldStatusName = oldStatus != null ? oldStatus.name() : "null";
            logs += String.format(" status updated from %s to %s;", oldStatusName, updated.getApplicationStatus().name());
        }
        if (!Objects.equals(oldRemarks, updated.getRemarks())) {
            logs += String.format(" remarks updated from '%s' to '%s';", oldRemarks, updated.getRemarks());
        }
        return logs;
    }
}