package de.hft.licensing.application.services;

import de.hft.licensing.application.repository.ApplicationDslService;
import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
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

    private final EmailService emailService;
    private final UserService userService;
    private final ApplicationDslService repository;

    public ApplicationService(EmailService emailService, UserService userService, ApplicationDslService repository) {
        this.emailService = emailService;
        this.userService = userService;
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

        NotificationPreferencesResource notificationPreferences = userService.getNotificationPreferences(UUID.fromString(updated.getUserId()));
        if (notificationPreferences == null) {
            NotificationPreferencesUpdate notificationPreferencesUpdate = new NotificationPreferencesUpdate();
            notificationPreferencesUpdate.setNotificationWay(NotificationWayApiEnum.EMAIL);
            notificationPreferencesUpdate.setApplicationUpdatesNotification(true);
            notificationPreferencesUpdate.setLicenseRenewalNotification(true);
            notificationPreferences = userService.updateNotificationPreferences(UUID.fromString(updated.getUserId()), notificationPreferencesUpdate);
            log.info("Initialized notification preferences for user with ID {}", updated.getUserId());
        }
        if(Boolean.TRUE.equals(notificationPreferences.getApplicationUpdatesNotification())
                && oldStatus != updated.getApplicationStatus() && notificationPreferences.getNotificationWay().equals(NotificationWayApiEnum.EMAIL)) {
            sendEmailOnStatusChange(updated);
        }

        userService.createNotification(
                applicationId,
                LocalDateTime.now(Clock.systemUTC()),
                UUID.fromString(updated.getUserId()),
                "The Application Status has been updated."
        );

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

    private void sendEmailOnStatusChange(ApplicationRecord updated) {
        UUID userId = UUID.fromString(updated.getUserId());
        int applicationId = updated.getId();
        if (userService.userExists(userId)) {
            UserResource user = userService.getUser(userId);
            boolean emailSent = emailService.sendEmail(user.getEmail(), "", "Your Application Status Has Changed", "The status of your application with the ID "+ applicationId +" has changed to " + updated.getApplicationStatus().name() + ".");
            if (emailSent) {
                log.info("Sent application status change email to user with ID {} for application ID {}.", userId, applicationId);
            } else {
                log.warn("Failed to send application status change email to user with ID {} for application ID {}.", userId, applicationId);
            }
        } else {
            log.warn("User with ID {} does not exist. Cannot send application status change email.", userId);
        }
    }
}