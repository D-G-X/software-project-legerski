package de.hft.licensing.application.services.routines;

import de.hft.licensing.application.services.ApplicationService;
import de.hft.licensing.application.services.EmailService;
import de.hft.licensing.application.services.UserService;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.application.repository.LicenseExpiryRoutineDslService;
import de.hft.licensing.model.UserResource;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class LicenseExpiryRoutine {

    private final LicenseExpiryRoutineDslService dslService;
    private static final Logger log = LicensingLoggerFactory.getLogger(LicenseExpiryRoutine.class);
    private final ApplicationService applicationService;
    private final UserService userService;
    private final EmailService emailService;

    public LicenseExpiryRoutine(LicenseExpiryRoutineDslService dslService, ApplicationService applicationService, UserService userService, EmailService emailService) {
        this.dslService = dslService;
        this.applicationService = applicationService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelayString = "${licensing.jobs.expire-licenses.fixed-delay-ms:300000}")
    @Transactional
    public void expireLicenses() {
        int updated = dslService.expireLicenses();
        log.info("[SCHEDULED TASK] Updated licenses: ({})", updated);
    }

    @Scheduled(cron = "${licensing.jobs.notify-expiring-licenses.cron:0 0 12 * * ?}", zone = "Europe/London")
    @Transactional
    public void notifyExpiringLicenses() {
        var expiringLicenses = dslService.getLicensesThatExpireSoon();
        log.info("[SCHEDULED TASK] Licenses expiring soon: ({})", expiringLicenses.size());

        for (var license : expiringLicenses) {
            log.info("License ID {} is expiring soon on {}", license.getId(), license.getExpiresAt());
            ApplicationRecord applicationRecord = applicationService.getApplication(license.getApplicationId());
            UUID userId = UUID.fromString(applicationRecord.getUserId());
            if (userService.userExists(userId)) {
                UserResource user = userService.getUser(userId);
                boolean emailSent = emailService.sendEmail(user.getEmail(), "", "Your License expires soon", "Your License with the ID "+ license.getId() +" will expire in 180 days.");
                if (emailSent) {
                    log.info("Sent license expiration email to user with ID {} for license ID {}.", userId, license.getId());
                } else {
                    log.warn("Failed to send application status change email to user with ID {} for application ID {}.", userId, license.getId());
                }
            } else {
                log.warn("User with ID {} does not exist. Cannot send application status change email.", userId);
            }
        }

    }
}