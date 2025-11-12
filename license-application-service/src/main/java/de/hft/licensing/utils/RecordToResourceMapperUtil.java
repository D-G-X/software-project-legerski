package de.hft.licensing.utils;

import de.hft.licensing.db.tables.records.ApplicationPaymentRecord;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import de.hft.licensing.db.tables.records.LicenseRecord;
import de.hft.licensing.db.tables.records.UserRecord;
import de.hft.licensing.model.ApplicationPaymentResource;
import de.hft.licensing.model.ApplicationResource;
import de.hft.licensing.model.LicenseResource;
import de.hft.licensing.model.UserResource;

import java.time.ZoneOffset;
import java.util.UUID;

public class RecordToResourceMapperUtil {

    public static void mapApplicationPaymentRecordToResource(ApplicationPaymentRecord applicationPaymentRecord, ApplicationPaymentResource applicationPaymentResource) {
        if (applicationPaymentRecord == null || applicationPaymentResource == null) {
            return;
        }
        applicationPaymentResource.setId(applicationPaymentRecord.getId());
        applicationPaymentResource.setApplicationId(applicationPaymentRecord.getApplicationId());
        applicationPaymentResource.setAmount(applicationPaymentRecord.getAmount());
        applicationPaymentResource.setPaymentDate(applicationPaymentRecord.getPaymentDate().atOffset(ZoneOffset.UTC));
        applicationPaymentResource.setPaymentStatus(EnumMapperUtil.getPendantFromEnum(applicationPaymentRecord.getPaymentStatus()));
    }

    public static void mapApplicationRecordToResource(ApplicationRecord applicationRecord, ApplicationResource applicationResource) {
        if(applicationRecord == null || applicationResource == null) {
            return;
        }
        applicationResource.setId(applicationRecord.getId());
        applicationResource.setUserId(UUID.fromString(applicationRecord.getUserId()));
        applicationResource.setCadastralReference(applicationRecord.getCadastralReference());
        applicationResource.setAppliedAt(applicationRecord.getAppliedAt().atOffset(ZoneOffset.UTC));
        applicationResource.setChangedAt(applicationRecord.getChangedAt().atOffset(ZoneOffset.UTC));
        applicationResource.setRemarks(applicationRecord.getRemarks());
        applicationResource.setLicenseType(EnumMapperUtil.getPendantFromEnum(applicationRecord.getLicenseType()));
    }

    public static void mapLicenseRecordToResource(LicenseRecord licenseRecord, LicenseResource licenseResource) {
        if (licenseRecord == null || licenseResource == null) {
            return;
        }
        licenseResource.setId(licenseRecord.getId());
        licenseResource.setUserId(UUID.fromString(licenseRecord.getUserId()));
        licenseResource.setApplicationId(licenseRecord.getApplicationId());
        licenseResource.setLicenseType(EnumMapperUtil.getPendantFromEnum(licenseRecord.getLicenseType()));
        licenseResource.setLicenseStatus(EnumMapperUtil.getPendantFromEnum(licenseRecord.getLicenseStatus()));
        licenseResource.setIssuedAt(licenseRecord.getIssuedAt().atOffset(ZoneOffset.UTC));
        licenseResource.setExpiresAt(licenseRecord.getExpiresAt().atOffset(ZoneOffset.UTC));
    }

    public static void mapUserRecordToResource(UserRecord userRecord, UserResource userResource) {
        if (userRecord == null || userResource == null) {
            return;
        }
        userResource.setId(UUID.fromString(userRecord.getId()));
        //userResource.setUsername(userRecord.getUsername());
        //userResource.setFirstName(userRecord.getFirstName());
        //userResource.setLastName(userRecord.getLastName());
        //userResource.setEmail(userRecord.getEmail());
        //userResource.setCreatedTimestamp(userRecord.getCreatedAt().atOffset(ZoneOffset.UTC));
        //userResource.setEnabled(userRecord.getEnabled());
        //userResource.setEmailVerified(userRecord.getEmailVerified());
    }

}
