package de.hft.licensing.rest;

import de.hft.licensing.api.AuthenticationApi;
import de.hft.licensing.model.LoginRequest;
import de.hft.licensing.model.LoginResource;
import de.hft.licensing.model.RefreshLoginRequest;
import de.hft.licensing.model.RegisterRequest;
import de.hft.licensing.model.RegisterResource;
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
    return ResponseEntity.created(URI.create("/auth/register/" + registerResource.getUserId())).

        build();
  }
}