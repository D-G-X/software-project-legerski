package de.hft.licensing.application.services.authServices;

import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("applicationAuthorization")
public class ApplicationAuthorizationService {

    private final DSLContext dsl;

    public ApplicationAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }


    /**
     * admin can do anything, normal uset may only request their own userId (or null which means myself)
     */
    public boolean canListApplications(Authentication authentication, UUID requestedUserId) {
        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        if (requestedUserId == null) {
            return true;
        }
        return requestedUserId.equals(currentUserId);
    }

    /**
     * only admin or owner of the application can access/update/delete
     */
    public boolean canAccessApplication(Authentication authentication, Integer applicationId) {
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

        ApplicationRecord record = dsl.selectFrom(Application.APPLICATION)
                .where(Application.APPLICATION.ID.eq(applicationId))
                .fetchOne();

        if (record == null) {
            // controller handles 404, but from auth perspective block
            return false;
        }

        UUID ownerId = UUID.fromString(record.getUserId());
        return currentUserId.equals(ownerId);
    }

    /**
     * allow normal user only if they create for themselves, admin can create for anyone.
     */
    public boolean canCreateApplication(Authentication authentication, UUID bodyUserId) {
        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }
        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        return currentUserId != null && currentUserId.equals(bodyUserId);
    }
}