package de.hft.licensing.application.services.authServices;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authenticationAuthorization")
public class AuthenticationAuthorizationService {

    public AuthenticationAuthorizationService() {
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
