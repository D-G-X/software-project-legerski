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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class LicenseController implements LicensesApi {

    private final DSLContext dsl;

    public LicenseController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ResponseEntity<Void> deleteLicense(Integer licenseId) {
        return null;
    }

    @Override
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
    public ResponseEntity<List<LicenseResource>> listLicenses(UUID userId) {
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
    public ResponseEntity<LicenseResource> updateLicenseStatus(Integer licenseId, UpdateLicenseStatusRequest updateLicenseStatusRequest) {
        if(licenseId == null || licenseId <= 0 || updateLicenseStatusRequest == null){
            return ResponseEntity.badRequest().build();
        }
        var dbLicense = dsl.update(License.LICENSE)
                .set(License.LICENSE.LICENSE_STATUS, (LicenseStatus) EnumMapperUtil.getPendantFromEnum(updateLicenseStatusRequest.getLicenseStatus()))
                .where(License.LICENSE.ID.eq(licenseId))
                .returning()
                .fetchOneInto(LicenseRecord.class);

        if(dbLicense != null){
            LicenseResource apiLicense = new LicenseResource();
            RecordToResourceMapperUtil.mapLicenseRecordToResource(dbLicense, apiLicense);
            return ResponseEntity.ok(apiLicense);
        }
        return ResponseEntity.notFound().build();
    }
}
