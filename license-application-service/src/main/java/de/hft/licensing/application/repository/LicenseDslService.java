package de.hft.licensing.application.repository;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.LicenseRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseDslService {

    private final DSLContext dsl;

    public LicenseDslService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record LicenseWithCadastralReference(LicenseRecord license, String cadastralReference) {
    }

    public int deleteLicense(Integer licenseId) {
        return dsl.deleteFrom(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .execute();
    }

    public LicenseWithCadastralReference getLicenseWithCadastral(Integer licenseId) {
        Record r = dsl.select(License.LICENSE.asterisk(), Application.APPLICATION.CADASTRAL_REFERENCE)
                .from(License.LICENSE)
                .leftJoin(Application.APPLICATION)
                .on(License.LICENSE.APPLICATION_ID.eq(Application.APPLICATION.ID))
                .where(License.LICENSE.ID.eq(licenseId))
                .fetchOne();

        if (r == null) {
            return null;
        }

        LicenseRecord licenseRecord = r.into(License.LICENSE).into(LicenseRecord.class);
        String cadastralReference = r.get(Application.APPLICATION.CADASTRAL_REFERENCE);

        return new LicenseWithCadastralReference(licenseRecord, cadastralReference);
    }

    public List<LicenseWithCadastralReference> listLicensesWithCadastralByUserId(String userId) {
        return dsl.select(License.LICENSE.asterisk(), Application.APPLICATION.CADASTRAL_REFERENCE)
                .from(License.LICENSE)
                .leftJoin(Application.APPLICATION)
                .on(License.LICENSE.APPLICATION_ID.eq(Application.APPLICATION.ID))
                .where(License.LICENSE.USER_ID.eq(userId))
                .fetch(r -> new LicenseWithCadastralReference(
                        r.into(License.LICENSE).into(LicenseRecord.class),
                        r.get(Application.APPLICATION.CADASTRAL_REFERENCE)
                ));
    }

    public List<LicenseWithCadastralReference> listAllLicensesWithCadastral() {
        return dsl.select(License.LICENSE.asterisk(), Application.APPLICATION.CADASTRAL_REFERENCE)
                .from(License.LICENSE)
                .leftJoin(Application.APPLICATION)
                .on(License.LICENSE.APPLICATION_ID.eq(Application.APPLICATION.ID))
                .fetch(r -> new LicenseWithCadastralReference(
                        r.into(License.LICENSE).into(LicenseRecord.class),
                        r.get(Application.APPLICATION.CADASTRAL_REFERENCE)
                ));
    }

    public LicenseStatus getLicenseStatus(Integer licenseId) {
        return dsl.select(License.LICENSE.LICENSE_STATUS)
                .from(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .fetchOneInto(License.LICENSE.LICENSE_STATUS.getType());
    }

    public LicenseRecord updateLicenseStatus(Integer licenseId, LicenseStatus newStatus) {
        return dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, newStatus)
                .where(License.LICENSE.ID.eq(licenseId))
                .returning()
                .fetchOneInto(LicenseRecord.class);
    }
}