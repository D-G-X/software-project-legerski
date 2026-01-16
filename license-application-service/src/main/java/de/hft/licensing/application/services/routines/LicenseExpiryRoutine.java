package de.hft.licensing.application.services.routines;

import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.application.repository.LicenseExpiryRoutineDslService;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LicenseExpiryRoutine {

    private final LicenseExpiryRoutineDslService dslService;
    private static final Logger log = LicensingLoggerFactory.getLogger(LicenseExpiryRoutine.class);

    public LicenseExpiryRoutine(LicenseExpiryRoutineDslService dslService) {
        this.dslService = dslService;
    }

    @Scheduled(fixedDelayString = "${licensing.jobs.expire-licenses.fixed-delay-ms:300000}")
    @Transactional
    public void expireLicenses() {
        int updated = dslService.expireLicenses();
        log.info("[SCHEDULED TASK] Updated licenses: ({})", updated);
    }
}