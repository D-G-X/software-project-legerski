package de.hft.licensing.utils;

import de.hft.licensing.db.tables.records.*;
import de.hft.licensing.model.*;

import java.time.ZoneOffset;
import java.util.UUID;

public class RecordToResourceMapperUtil {

    public static void mapApplicationPaymentRecordToResource(ApplicationPaymentRecord r, ApplicationPaymentResource out) {
        if (r == null || out == null) return;

        out.setId(r.getId());
        out.setApplicationId(r.getApplicationId());
        out.setAmount(r.getAmount());

        if (r.getPaymentDate() != null) {
            out.setPaymentDate(r.getPaymentDate().atOffset(ZoneOffset.UTC));
        }
        if (r.getPaymentStatus() != null) {
            out.setPaymentStatus(EnumMapperUtil.getPendantFromEnum(r.getPaymentStatus()));
        }

        out.setName(r.getAccountant());
        out.setIban(r.getIban());
        out.setBic(r.getBic());
    }

    public static void mapApplicationRecordToResource(ApplicationRecord r, ApplicationResource out) {
        if (r == null || out == null) return;

        out.setId(r.getId());
        out.setCadastralReference(r.getCadastralReference());
        out.setRemarks(r.getRemarks());

        if (r.getUserId() != null) {
            try {
                out.setUserId(UUID.fromString(r.getUserId()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (r.getAppliedAt() != null) {
            out.setAppliedAt(r.getAppliedAt().atOffset(ZoneOffset.UTC));
        }
        if (r.getChangedAt() != null) {
            out.setChangedAt(r.getChangedAt().atOffset(ZoneOffset.UTC));
        }

        if (r.getLicenseType() != null) {
            out.setLicenseType(EnumMapperUtil.getPendantFromEnum(r.getLicenseType()));
        }
        if (r.getApplicationStatus() != null) {
            out.setApplicationStatus(EnumMapperUtil.getPendantFromEnum(r.getApplicationStatus()));
        }
    }

    public static void mapLicenseRecordToResource(LicenseRecord r, LicenseResource out) {
        if (r == null || out == null) return;

        out.setId(r.getId());
        out.setApplicationId(r.getApplicationId());

        if (r.getUserId() != null) {
            try {
                out.setUserId(UUID.fromString(r.getUserId()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (r.getLicenseType() != null) {
            out.setLicenseType(EnumMapperUtil.getPendantFromEnum(r.getLicenseType()));
        }
        if (r.getLicenseStatus() != null) {
            out.setLicenseStatus(EnumMapperUtil.getPendantFromEnum(r.getLicenseStatus()));
        }
        if (r.getIssuedAt() != null) {
            out.setIssuedAt(r.getIssuedAt().atOffset(ZoneOffset.UTC));
        }
        if (r.getExpiresAt() != null) {
            out.setExpiresAt(r.getExpiresAt().atOffset(ZoneOffset.UTC));
        }
    }

    public static void mapUserRecordToResource(UserRecord r, UserResource out) {
        if (r == null || out == null) return;

        if (r.getId() != null) {
            try {
                out.setId(UUID.fromString(r.getId()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static void mapBallotPeriodRecordToResource(BallotPeriodRecord r, BallotPeriodResource out) {
        if (r == null || out == null) return;

        out.setBallotPeriodId(r.getId());

        if (r.getStartDate() != null) {
            out.setStartDate(r.getStartDate().atOffset(ZoneOffset.UTC));
        }
        if (r.getEndDate() != null) {
            out.setEndDate(r.getEndDate().atOffset(ZoneOffset.UTC));
        }
    }

    public static void mapCurrentBallotPeriodRecordToResource(BallotPeriodRecord r, CurrentBallotPeriodResource out) {
        if (r == null || out == null) return;

        if (r.getStartDate() != null) {
            out.setStartDate(r.getStartDate().atOffset(ZoneOffset.UTC));
        }
        if (r.getEndDate() != null) {
            out.setEndDate(r.getEndDate().atOffset(ZoneOffset.UTC));
        }
    }

    public static void mapNotificationPreferencesRecordToResource(NotificationPreferencesRecord r, NotificationPreferencesResource out) {
        if (r == null || out == null) return;

        if (r.getUserId() != null) {
            try {
                out.setUserId(UUID.fromString(r.getUserId()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (r.getNotificationWay() != null) {
            out.setNotificationWay(EnumMapperUtil.getPendantFromEnum(r.getNotificationWay()));
        }

        if (r.getApplicationUpdatesNotification() != null) {
            out.setApplicationUpdatesNotification(r.getApplicationUpdatesNotification());
        }
        if (r.getLicenseRenewalNotification() != null) {
            out.setLicenseRenewalNotification(r.getLicenseRenewalNotification());
        }
    }
}