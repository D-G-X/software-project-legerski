package de.hft.licensing.application.services.authServices;

import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("paymentAuthorization")
public class PaymentAuthorizationService {

    private final DSLContext dsl;

    public PaymentAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * only admin or owner of the application can access payments
     */
    public boolean canAccessPayments(Authentication authentication, Integer applicationId) {
        if (applicationId == null) {
            return false;
        }

        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        ApplicationRecord applicationRecord = dsl
                .selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOne();

        if (applicationRecord == null) {
            return false;
        }

        UUID ownerId;
        try {
            ownerId = UUID.fromString(applicationRecord.getUserId());
        } catch (IllegalArgumentException e) {
            return false;
        }

        return currentUserId.equals(ownerId);
    }

}