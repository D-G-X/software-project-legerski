package de.hft.licensing.rest;

import de.hft.licensing.api.AuthenticationApi;
import de.hft.licensing.model.*;
import de.hft.licensing.services.KeycloakAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<LoginResource> loginUser(LoginRequest loginRequest) {
        return ResponseEntity.ok().body(authService.login(loginRequest));
    }

    @Override
    @Transactional
    public ResponseEntity<RegisterResource> registerUser(RegisterRequest registerRequest) {
        RegisterResource registerResource = authService.register(registerRequest);
        if(registerResource == null) {
            return ResponseEntity.status(409).build();
        } else if (registerResource.getUserId() == null) {
            return ResponseEntity.badRequest().body(registerResource);
        }
        return ResponseEntity.created(URI.create("/auth/register/" + registerResource.getUserId())).build();
    }
}