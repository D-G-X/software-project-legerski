package de.hft.licensing.application.services;

import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.tables.records.LicenseRecord;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.UpdateLicenseStatusRequest;
import de.hft.licensing.application.repository.LicenseDslService;
import de.hft.licensing.utils.EnumMapperUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LicenseService {

    private static final Logger log = LicensingLoggerFactory.getLogger(LicenseService.class);

    private final LicenseDslService repository;

    public LicenseService(LicenseDslService repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean deleteLicense(Integer licenseId) {
        int deletedRows = repository.deleteLicense(licenseId);

        if (deletedRows > 0) {
            log.info("Deleted license with ID: {}", licenseId);
            return true;
        } else {
            log.warn("Attempted to delete non-existing license with ID: {}", licenseId);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public LicenseDslService.LicenseWithCadastralReference getLicenseWithCadastral(Integer licenseId) {
        return repository.getLicenseWithCadastral(licenseId);
    }

    @Transactional(readOnly = true)
    public List<LicenseDslService.LicenseWithCadastralReference> listLicensesWithCadastral(UUID userId) {
        if (userId != null) {
            return repository.listLicensesWithCadastralByUserId(userId.toString());
        }
        return repository.listAllLicensesWithCadastral();
    }

    @Transactional
    public LicenseRecord updateLicenseStatus(Integer licenseId, UpdateLicenseStatusRequest req) {
        LicenseStatus oldStatus = repository.getLicenseStatus(licenseId);

        LicenseStatus newStatus = EnumMapperUtil.getPendantFromEnum(req.getLicenseStatus());
        LicenseRecord dbLicense = repository.updateLicenseStatus(licenseId, newStatus);

        if (dbLicense == null) {
            return null;
        }

        String logs = String.format("Updated license with ID %d:", licenseId);
        if (oldStatus != dbLicense.getLicenseStatus()) {
            String oldStatusName = oldStatus != null ? oldStatus.name() : "null";
            logs += String.format(" license status changed from %s to %s.", oldStatusName, dbLicense.getLicenseStatus().name());
        }
        log.info(logs);

        return dbLicense;
    }
}