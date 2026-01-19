package de.hft.licensing.application.restController;

import de.hft.licensing.api.AuthenticationApi;
import de.hft.licensing.logger.LicensingLoggerFactory;
import de.hft.licensing.model.*;
import de.hft.licensing.application.services.AuthService;
import de.hft.licensing.utils.ApiFormValidator;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class AuthController implements AuthenticationApi {

    private final AuthService authService;
    private final ApiFormValidator formValidator = new ApiFormValidator();
    private final Logger log = LicensingLoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<LoginResource> loginUser(LoginRequest loginRequest) {
        if (loginRequest.getEmail() != null &&
                loginRequest.getPassword() != null &&
                !formValidator.isValidEmail(loginRequest.getEmail()) &&
                !formValidator.isValidPassword(loginRequest.getPassword())) {
            log.warn("Login attempt with invalid email or password format for email: {}", loginRequest.getEmail());
            return ResponseEntity.badRequest().build();
        }
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        return ResponseEntity.ok().body(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<LoginResource> refreshLogin(RefreshLoginRequest refreshLoginRequest) {
        return ResponseEntity.ok().body(authService.refreshLogin(refreshLoginRequest));
    }

    @Override
    public ResponseEntity<RegisterResource> registerUser(RegisterRequest registerRequest) {
        RegisterResource registerResource = authService.register(registerRequest);
        if (registerResource == null) {
            log.warn("Unknown registration attempt");
            return ResponseEntity.status(409).build();
        } else if (registerRequest.getFirstname() != null &&
                registerRequest.getLastname() != null &&
                registerRequest.getEmail() != null &&
                registerRequest.getPassword() != null &&
                !formValidator.isValidName(registerRequest.getFirstname()) &&
                !formValidator.isValidName(registerRequest.getLastname()) &&
                !formValidator.isValidEmail(registerRequest.getEmail()) &&
                !formValidator.isValidPassword(registerRequest.getPassword())) {
            log.warn("Registration attempt for email failed: {}", registerRequest.getEmail());
            return ResponseEntity.badRequest().build();
        } else if (registerResource.getUserId() == null) {
            log.warn("Registration attempt for email failed: {}", registerRequest.getEmail());
            return ResponseEntity.badRequest().body(registerResource);
        }
        log.info("Registration attempt for email: {}", registerRequest.getEmail());
        return ResponseEntity.created(URI.create("/auth/register/" + registerResource.getUserId())).build();
    }

    @Override
    public ResponseEntity<Void> requestPasswordReset(ResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest == null || resetPasswordRequest.getEmail() == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthService.RequestPasswordResetResult res = authService.requestPasswordReset(resetPasswordRequest.getEmail());

        if (res == AuthService.RequestPasswordResetResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        if (res == AuthService.RequestPasswordResetResult.INTERNAL_ERROR) {
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("@authenticationAuthorization.userIsUserOrAdmin(authentication, #userId)")
    public ResponseEntity<Void> changeUserDetails(UUID userId, ChangeUserdetailsRequest changeUserdetailsRequest) {
        if (userId == null || changeUserdetailsRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthService.ChangeUserDetailsResult res = authService.changeUserDetails(userId, changeUserdetailsRequest);

        if (res == AuthService.ChangeUserDetailsResult.CONFLICT) {
            return ResponseEntity.status(409).build();
        }
        if (res == AuthService.ChangeUserDetailsResult.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }
        if (res == AuthService.ChangeUserDetailsResult.FAILED) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> changeUserPassword(ChangePasswordRequest changePasswordRequest) {
        if (changePasswordRequest == null || changePasswordRequest.getToken() == null || changePasswordRequest.getNewPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthService.ChangePasswordResult res = authService.changeUserPassword(changePasswordRequest.getToken(), changePasswordRequest.getNewPassword());

        if (res == AuthService.ChangePasswordResult.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }
        if (res == AuthService.ChangePasswordResult.INTERNAL_ERROR) {
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }
}