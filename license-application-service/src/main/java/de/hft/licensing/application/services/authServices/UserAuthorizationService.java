package de.hft.licensing.application.services.authServices;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAuthorization")
public class UserAuthorizationService {

    public boolean canAccessUser(Authentication authentication, UUID requestUserId) {
        if (requestUserId == null) {
            return false;
        }

        if (CommonAuthorizationService.isAdmin(authentication)) {
            return true;
        }

        UUID currentUserId = CommonAuthorizationService.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        return currentUserId.equals(requestUserId);
    }
}
