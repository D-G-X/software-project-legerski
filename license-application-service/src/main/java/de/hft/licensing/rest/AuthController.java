package de.hft.licensing.rest;

import de.hft.licensing.api.AuthenticationApi;
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