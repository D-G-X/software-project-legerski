package de.hft.licensing.application.services.authServices;

import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.LicenseRecord;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("licenseAuthorization")
public class LicenseAuthorizationService {

    private final DSLContext dsl;

    public LicenseAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean canAccessLicense(Authentication authentication, Integer licenseId) {
        if (licenseId == null) {
            return false;
        }

        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        LicenseRecord record = dsl.selectFrom(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .fetchOne();

        if (record == null) {
            // controller handles 404, but from auth perspective block
            return false;
        }

        UUID ownerId = UUID.fromString(record.getUserId());
        return currentUserId.equals(ownerId);
    }

    public boolean canListLicenses(Authentication authentication, UUID requestedUserId) {
        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        } else if (requestedUserId == null) {
            return true;
        }
        return requestedUserId.equals(currentUserId);
    }
}
