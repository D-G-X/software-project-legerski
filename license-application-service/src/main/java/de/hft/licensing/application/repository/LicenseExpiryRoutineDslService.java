package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class LicenseExpiryRoutineDslService {

    private final DefaultDSLContext dsl;

    public LicenseExpiryRoutineDslService(DefaultDSLContext dsl) {
        this.dsl = dsl;
    }

    public int expireLicenses() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        return dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, LicenseStatus.expired)
                .where(License.LICENSE.LICENSE_STATUS.eq(LicenseStatus.active))
                .and(License.LICENSE.EXPIRES_AT.le(now))
                .execute();
    }
}
