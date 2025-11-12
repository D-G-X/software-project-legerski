package de.hft.licensing.utils;

import de.hft.licensing.db.enums.ApplicationStatus;
import de.hft.licensing.db.enums.LicenseStatus;
import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.enums.PaymentStatus;
import de.hft.licensing.model.ApplicationStatusEnum;
import de.hft.licensing.model.LicenseStatusEnum;
import de.hft.licensing.model.LicenseTypeEnum;
import de.hft.licensing.model.PaymentStatusEnum;

public class EnumMapperUtil {

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>, R extends Enum<?>> R getPendantFromEnum(T enumValue) {
        if (enumValue == null) return null;
        return (R) switch (enumValue) {
            case ApplicationStatusEnum e -> ApplicationStatus.lookupLiteral(e.getValue());
            case ApplicationStatus e -> ApplicationStatusEnum.fromValue(e.getLiteral());
            case LicenseTypeEnum e -> LicenseType.lookupLiteral(e.getValue());
            case LicenseType e -> LicenseTypeEnum.fromValue(e.getLiteral());
            case LicenseStatusEnum e -> LicenseStatus.lookupLiteral(e.getValue());
            case LicenseStatus e -> LicenseStatusEnum.fromValue(e.getLiteral());
            case PaymentStatusEnum e -> PaymentStatus.lookupLiteral(e.getValue());
            case PaymentStatus e -> PaymentStatusEnum.fromValue(e.getLiteral());
            default -> null;
        };
    }

}
