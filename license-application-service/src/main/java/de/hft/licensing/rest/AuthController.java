package de.hft.licensing.rest;

import de.hft.licensing.api.AuthenticationApi;
import de.hft.licensing.model.*;
import de.hft.licensing.services.EmailService;
import de.hft.licensing.services.KeycloakAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@CrossOrigin(origins = "*")
public class AuthController implements AuthenticationApi {

    private final KeycloakAuthService authService;

    public AuthController(KeycloakAuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<LoginResource> loginUser(LoginRequest loginRequest) {
        return ResponseEntity.ok().body(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<RegisterResource> registerUser(RegisterRequest registerRequest) {
        RegisterResource registerResource = authService.register(registerRequest);
        if(registerResource == null) {
            return ResponseEntity.status(409).build();
        } else if (registerResource.getUserId() == null) {
            return ResponseEntity.badRequest().body(registerResource);
        }
        return ResponseEntity.created(URI.create("/auth/register/" + registerResource.getUserId())).build();
    }

    @Override
    public ResponseEntity<Void> requestPasswordReset(ResetPasswordRequest resetPasswordRequest) {
        // Check email validity
        boolean isEmailRegistered = authService.isEmailRegistered(resetPasswordRequest.getEmail());
        if (!isEmailRegistered) {
            System.out.println("Password reset requested for unregistered email: " + resetPasswordRequest.getEmail());
            return ResponseEntity.notFound().build();
        }

        // Send email
        boolean isEmailSent = EmailService.sendEmail(
            resetPasswordRequest.getEmail(),
            null,
            "Password Reset Request",
            "Click the link to reset your password: <a href=\"https://example.com/reset?email=" + resetPasswordRequest.getEmail() + "\">Reset Password</a>"
        );
        if (!isEmailSent) {
            System.out.println("Failed to send password reset email to: " + resetPasswordRequest.getEmail());
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> changeUserPassword(ChangePasswordRequest changePasswordRequest) {
        boolean isPasswordChanged = authService.changePassword(
            changePasswordRequest.getEmail(),
            changePasswordRequest.getNewPassword()
        );
        if (!isPasswordChanged) {
            System.out.println("Failed to change password for email: " + changePasswordRequest.getEmail());
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }
}