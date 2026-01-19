package de.hft.licensing.application.services.authServices;

import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("preferencesAuthorization")
public class PreferencesAuthorizationService {

    private final DSLContext dsl;

    public PreferencesAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean canAccessPreferences(Authentication authentication, UUID requestedUserId) {
        if (requestedUserId == null) {
            return false;
        }

        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        return requestedUserId.equals(currentUserId);

    }
}
