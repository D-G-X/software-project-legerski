package de.hft.licensing.utils.auth;

public class RegisterRessource {
    final private String userId;

    public RegisterRessource(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
