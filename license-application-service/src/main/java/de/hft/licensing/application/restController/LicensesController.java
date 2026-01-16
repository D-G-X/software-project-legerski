package de.hft.licensing.application.restController;

import de.hft.licensing.api.LicensesApi;
import de.hft.licensing.model.LicenseResource;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
import de.hft.licensing.application.services.LicenseService;
import de.hft.licensing.utils.RecordToResourceMapperUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class LicensesController implements LicensesApi {

    private final LicenseService licenseService;

    public LicensesController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<Void> deleteLicense(Integer licenseId) {
        if (licenseId == null || licenseId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = licenseService.deleteLicense(licenseId);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<LicenseResource> getLicense(Integer licenseId) {
        if (licenseId == null || licenseId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var joined = licenseService.getLicenseWithCadastral(licenseId);
        if (joined == null || joined.license() == null) {
            return ResponseEntity.notFound().build();
        }

        LicenseResource apiLicense = new LicenseResource();
        RecordToResourceMapperUtil.mapLicenseRecordToResource(joined.license(), apiLicense);

        if (joined.cadastralReference() != null) {
            apiLicense.setCadastralReference(joined.cadastralReference());
        }

        return ResponseEntity.ok(apiLicense);
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canListLicenses(authentication, #userId)")
    public ResponseEntity<List<LicenseResource>> listLicenses(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = jwt.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

        UUID currentUserId = UUID.fromString(jwt.getToken().getSubject());

        if (userId != null && !isAdmin && !userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (!isAdmin) {
            userId = currentUserId;
        }

        var joined = licenseService.listLicensesWithCadastral(userId);

        if (joined == null || joined.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<LicenseResource> apiLicenses = new ArrayList<>(joined.size());
        for (var row : joined) {
            LicenseResource apiLicense = new LicenseResource();
            RecordToResourceMapperUtil.mapLicenseRecordToResource(row.license(), apiLicense);

            if (row.cadastralReference() != null) {
                apiLicense.setCadastralReference(row.cadastralReference());
            }
            apiLicenses.add(apiLicense);
        }

        return ResponseEntity.ok(apiLicenses);
    }

    @Override
    @PreAuthorize("@licenseAuthorization.canAccessLicense(authentication, #licenseId)")
    public ResponseEntity<LicenseResource> updateLicenseStatus(Integer licenseId, UpdateLicenseStatusRequest updateLicenseStatusRequest) {
        if (licenseId == null || licenseId <= 0 || updateLicenseStatusRequest == null || updateLicenseStatusRequest.getLicenseStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        var updated = licenseService.updateLicenseStatus(licenseId, updateLicenseStatusRequest);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        LicenseResource apiLicense = new LicenseResource();
        RecordToResourceMapperUtil.mapLicenseRecordToResource(updated, apiLicense);
        return ResponseEntity.ok(apiLicense);
    }
}