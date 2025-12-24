package de.hft.licensing.services.dslService;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class SchedulerDslService {

    private final DefaultDSLContext dsl;

    public SchedulerDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    public int expireLicenses() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        return dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, LicenseStatus.expired)
                .where(License.LICENSE.LICENSE_STATUS.eq(LicenseStatus.active))
                .and(License.LICENSE.EXPIRES_AT.le(now))
                .execute();
    }
}
