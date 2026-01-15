package de.hft.licensing.services;

import de.hft.licensing.db.enums.LicenseType;
import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentsControllerService {

    private static final int ETV_amount = 3500;
    private static final int ETVPL_amount = 875;
    private static final int ETV60_amount = 290;

    private final DSLContext dslContext;

    public PaymentsControllerService(DSLContext dslContext) {

        this.dslContext = dslContext;
    }

    public BigDecimal calculateFeeAmount(Integer applicationId) {
        ApplicationRecord appRecord = dslContext.select()
                .from(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOneInto(ApplicationRecord.class);

        LicenseType applicationLicenseType = appRecord.getLicenseType();
        switch (applicationLicenseType) {
            case etv -> {
                return new BigDecimal(ETV_amount);
            }
            case etvpl -> {
                return new BigDecimal(ETVPL_amount);
            }
            case etv60 -> {
                return new BigDecimal(ETV60_amount);
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }
}
