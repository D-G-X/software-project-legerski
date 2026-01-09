package de.hft.licensing.rest;

import de.hft.licensing.api.AuthenticationApi;
import de.hft.licensing.db.tables.records.PasswordResetTokenRecord;
import de.hft.licensing.model.ChangePasswordRequest;
import de.hft.licensing.model.LoginRequest;
import de.hft.licensing.model.LoginResource;
import de.hft.licensing.model.RefreshLoginRequest;
import de.hft.licensing.model.RegisterRequest;
import de.hft.licensing.model.RegisterResource;
import de.hft.licensing.model.ResetPasswordRequest;
import de.hft.licensing.services.EmailService;
import de.hft.licensing.services.KeycloakAuthService;
import de.hft.licensing.utils.ApiFormValidator;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AuthController implements AuthenticationApi {

  private final KeycloakAuthService authService;

  private final ApiFormValidator formValidator = new ApiFormValidator();

  public AuthController(KeycloakAuthService authService) {
    this.authService = authService;
  }

  @Override
  @Transactional
  public ResponseEntity<LoginResource> loginUser(LoginRequest loginRequest) {
    if (loginRequest.getEmail() != null &&
        loginRequest.getPassword() != null &&
        !formValidator.isValidEmail(loginRequest.getEmail()) &&
        !formValidator.isValidPassword(loginRequest.getPassword())) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok().body(authService.login(loginRequest));
  }

  @Override
  @Transactional
  public ResponseEntity<LoginResource> refreshLogin(RefreshLoginRequest refreshLoginRequest) {
    return ResponseEntity.ok().body(authService.refreshLogin(refreshLoginRequest));
  }

  @Override
  @Transactional
  public ResponseEntity<RegisterResource> registerUser(RegisterRequest registerRequest) {
    RegisterResource registerResource = authService.register(registerRequest);
    if (registerResource == null) {
      return ResponseEntity.status(409).build();
    } else if (registerRequest.getFirstname() != null &&
        registerRequest.getLastname() != null &&
        registerRequest.getEmail() != null &&
        registerRequest.getPassword() != null &&
        !formValidator.isValidName(registerRequest.getFirstname()) &&
        !formValidator.isValidName(registerRequest.getLastname()) &&
        !formValidator.isValidEmail(registerRequest.getEmail()) &&
        !formValidator.isValidPassword(registerRequest.getPassword())) {
      return ResponseEntity.badRequest().build();
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
        UUID userId = authService.getUserIdByEmail(resetPasswordRequest.getEmail());
        if (userId == null) {
            System.out.println("No user found in Keycloak with email " + resetPasswordRequest.getEmail());
            return ResponseEntity.notFound().build();
        }

        // Create user token
        String token = authService.createPasswordResetToken(resetPasswordRequest.getEmail());
        if (token == null) {
            System.out.println("Failed to create password reset token for email: " + resetPasswordRequest.getEmail());
            return ResponseEntity.status(500).build();
        }
        // Create token record
        boolean isTokenRecordCreated = authService.storePasswordResetToken(
            userId,
            token
        );
        if (!isTokenRecordCreated) {
            System.out.println("Failed to store password reset token for email: " + resetPasswordRequest.getEmail());
            return ResponseEntity.status(500).build();
        }

        // Send email
        boolean isEmailSent = EmailService.sendEmail(
            resetPasswordRequest.getEmail(),
            null,
            "Password Reset Request",
            "Click the link to reset your password: <a href=\"http://localhost:3000/reset-password?token=" + token + "\">Reset Password</a>"
        );
        if (!isEmailSent) {
            System.out.println("Failed to send password reset email to: " + resetPasswordRequest.getEmail());
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> changeUserPassword(ChangePasswordRequest changePasswordRequest) {
        PasswordResetTokenRecord tokenRecord = authService.getPasswordResetTokenRecord(changePasswordRequest.getToken());
        // check token validity
        // - exists
        if (tokenRecord == null) {
            System.out.println("Invalid password reset token: " + changePasswordRequest.getToken());
            return ResponseEntity.badRequest().build();
        }
        // - not expired
        Date now = new Date();
        Date expiresAt = java.sql.Timestamp.valueOf(tokenRecord.getExpiresAt());
        if (now.after(expiresAt)) {
            System.out.println("Expired password reset token for user ID: " + tokenRecord.getUserId());
            return ResponseEntity.badRequest().build();
        }
        // - not used
        if (tokenRecord.getUsed()) {
            System.out.println("Already used password reset token for user ID: " + tokenRecord.getUserId());
            return ResponseEntity.badRequest().build();
        }

        String userEmail = authService.getEmailByUserId(UUID.fromString(tokenRecord.getUserId()));
        boolean isPasswordChanged = authService.changePassword(
            userEmail,
            changePasswordRequest.getNewPassword()
        );
        if (!isPasswordChanged) {
            System.out.println("Failed to change password for email: " + userEmail);
            return ResponseEntity.status(500).build();
        }

        // mark token as used
        boolean isTokenMarkedUsed = authService.markPasswordResetTokenAsUsed(changePasswordRequest.getToken());
        if (!isTokenMarkedUsed) {
            System.out.println("Failed to mark password reset token as used for token: " + changePasswordRequest.getToken());
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }
}
