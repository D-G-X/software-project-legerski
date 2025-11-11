package de.hft.licensing.rest;

import de.hft.licensing.api.LicensesApi;

import de.hft.licensing.model.LicenseResource;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
        return null;
    }

    @Override
    public ResponseEntity<List<LicenseResource>> listLicenses(UUID userId) {
        return null;
    }

    @Override
    public ResponseEntity<LicenseResource> updateLicenseStatus(Integer licenseId, UpdateLicenseStatusRequest updateLicenseStatusRequest) {
        return null;
    }
}
