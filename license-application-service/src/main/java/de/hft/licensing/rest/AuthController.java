package de.hft.licensing.rest;

import de.hft.licensing.services.KeycloakAuthService;
import de.hft.licensing.utils.auth.LoginRequest;
import de.hft.licensing.utils.auth.LoginRessource;
import de.hft.licensing.utils.auth.RegisterRequest;
import de.hft.licensing.utils.auth.RegisterRessource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private KeycloakAuthService authService;

    @PostMapping("/login")
    public LoginRessource login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public RegisterRessource register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}