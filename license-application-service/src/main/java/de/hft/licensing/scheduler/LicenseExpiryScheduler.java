package de.hft.licensing.scheduler;

import de.hft.licensing.services.dslService.SchedulerDslService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LicenseExpiryScheduler {

    private final SchedulerDslService dslService;

    public LicenseExpiryScheduler(SchedulerDslService dslService) {
        this.dslService = dslService;
    }

    @Scheduled(fixedDelayString = "${licensing.jobs.expire-licenses.fixed-delay-ms:300000}")
    @Transactional
    public void expireLicenses() {
        int updated = dslService.expireLicenses();
        System.out.println("[SCHEDULER]: - Updated licenses: (" + updated + ")");
    }
}