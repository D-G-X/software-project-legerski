package de.hft.licensing.services.auth;


import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authenticationAuthorization")
public class AuthenticationAuthorizationService {

    private final DSLContext dsl;

    public AuthenticationAuthorizationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean userIsUserOrAdmin(Authentication authentication, UUID requestedUserId) {
        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null || requestedUserId == null) {
            return false;
        }

        return requestedUserId.equals(currentUserId);
    }

}
