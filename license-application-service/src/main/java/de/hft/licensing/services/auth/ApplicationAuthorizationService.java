package de.hft.licensing.services.auth;

import de.hft.licensing.db.tables.Application;
import de.hft.licensing.db.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("applicationAuthorization")
public class ApplicationAuthorizationService {

    private final DSLContext dsl;

    public ApplicationAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    private boolean isAdmin(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !authentication.isAuthenticated()) {
            return false;
        }
        return jwt.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin"));
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return null;
        }
        try {
            return UUID.fromString(jwt.getToken().getSubject());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * admin can do anything, normal uset may only request their own userId (or null which means myself)
     */
    public boolean canListApplications(Authentication authentication, UUID requestedUserId) {
        if (isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = getCurrentUserId(authentication);
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

        if (isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = getCurrentUserId(authentication);
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
        if (isAdmin(authentication)) {
            return true;
        }
        UUID currentUserId = getCurrentUserId(authentication);
        return currentUserId != null && currentUserId.equals(bodyUserId);
    }
}