package de.hft.licensing.utils.auth;

public class RegisterResponse {
    final private String userId;

    public RegisterResponse(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
