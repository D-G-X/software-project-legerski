package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.LicenseRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

    public List<LicenseRecord> getLicensesThatExpireSoon() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")).plusDays(180);
        return dsl.selectFrom(License.LICENSE)
                .where(License.LICENSE.LICENSE_STATUS.eq(LicenseStatus.active))
                .and(License.LICENSE.EXPIRES_AT.greaterThan(now))
                .fetchInto(LicenseRecord.class);

    }
}
