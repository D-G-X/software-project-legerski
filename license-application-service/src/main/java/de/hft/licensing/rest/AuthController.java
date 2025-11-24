package de.hft.licensing.rest;

import de.hft.licensing.services.KeycloakAuthService;
import de.hft.licensing.utils.auth.LoginRequest;
import de.hft.licensing.utils.auth.LoginResponse;
import de.hft.licensing.utils.auth.RegisterRequest;
import de.hft.licensing.utils.auth.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // maybe optional. Check it
public class AuthController {

    @Autowired
    private KeycloakAuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}