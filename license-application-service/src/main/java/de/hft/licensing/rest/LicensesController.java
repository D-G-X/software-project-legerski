package de.hft.licensing.rest;

import de.hft.licensing.api.LicensesApi;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.License;
import de.hft.licensing.db.tables.records.LicenseRecord;
import de.hft.licensing.model.LicenseResource;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
import de.hft.licensing.utils.EnumMapperUtil;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class LicensesController implements LicensesApi {

    private final DSLContext dsl;
    private static final Logger log = LoggerFactory.getLogger(LicensesController.class);

    public LicensesController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<Void> deleteLicense(Integer licenseId) {
        if(licenseId == null || licenseId <= 0){
            return ResponseEntity.badRequest().build();
        }

        int deletedRows = dsl.deleteFrom(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .execute();

        if(deletedRows > 0){
            log.info("Deleted license with ID: {}", licenseId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Attempted to delete non-existing license with ID: {}", licenseId);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<LicenseResource> getLicense(Integer licenseId) {
        if(licenseId == null || licenseId <= 0){
            return ResponseEntity.badRequest().build();
        }

        var dbLicense = dsl.select()
                .from(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .fetchOneInto(LicenseRecord.class);

        if (dbLicense != null){
            LicenseResource apiLicense = new LicenseResource();
            RecordToResourceMapperUtil.mapLicenseRecordToResource(dbLicense, apiLicense);
            return ResponseEntity.ok(apiLicense);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canListLicenses(authentication, #userId)")
    public ResponseEntity<List<LicenseResource>> listLicenses(UUID userId) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Maps roles from JWT token
        boolean isAdmin = jwt.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
        // Extract the user ID from Keycloak token: "sub" claim
        UUID currentUserId = UUID.fromString(jwt.getToken().getSubject());
        // Enforce: normal users can only see their own applications
        if (userId != null && !isAdmin && !userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (!isAdmin) {
            userId = currentUserId;
        }

        List<LicenseRecord> dbLicenses = new ArrayList<>();
        List<LicenseResource> apiLicenses = new ArrayList<>();

        if(userId != null) {
            dbLicenses = dsl.select()
                    .from(License.LICENSE)
                    .where(License.LICENSE.USER_ID.eq(userId.toString()))
                    .fetchInto(LicenseRecord.class);
        } else {
            dbLicenses = dsl.select()
                    .from(License.LICENSE)
                    .fetchInto(LicenseRecord.class);
        }

        for(LicenseRecord dbLicense : dbLicenses){
            LicenseResource apiLicense = new LicenseResource();
            RecordToResourceMapperUtil.mapLicenseRecordToResource(dbLicense, apiLicense);
            apiLicenses.add(apiLicense);
        }

        return !apiLicenses.isEmpty()
                ? ResponseEntity.ok(apiLicenses)
                : ResponseEntity.notFound().build();
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<LicenseResource> updateLicenseStatus(Integer licenseId, UpdateLicenseStatusRequest updateLicenseStatusRequest) {
        if(licenseId == null || licenseId <= 0 || updateLicenseStatusRequest == null){
            return ResponseEntity.badRequest().build();
        }

        var oldStatus = dsl.select(License.LICENSE.LICENSE_STATUS)
                .from(License.LICENSE)
                .where(License.LICENSE.ID.eq(licenseId))
                .fetchOneInto(License.LICENSE.LICENSE_STATUS.getType());

        var dbLicense = dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, (LicenseStatus) EnumMapperUtil.getPendantFromEnum(updateLicenseStatusRequest.getLicenseStatus()))
                .where(License.LICENSE.ID.eq(licenseId))
                .returning()
                .fetchOneInto(LicenseRecord.class);

        if(dbLicense != null){
            // Logger
            var logs = String.format("Updated license with ID %d:", licenseId);
            if (oldStatus != dbLicense.getLicenseStatus()){
                var oldStatusName = oldStatus != null ? oldStatus.name() : "null";
                logs += String.format(" license status changed from %s to %s.", oldStatusName, dbLicense.getLicenseStatus().name());
            }
            log.info(logs);

            LicenseResource apiLicense = new LicenseResource();
            RecordToResourceMapperUtil.mapLicenseRecordToResource(dbLicense, apiLicense);
            return ResponseEntity.ok(apiLicense);
        }
        return ResponseEntity.notFound().build();
    }
}
