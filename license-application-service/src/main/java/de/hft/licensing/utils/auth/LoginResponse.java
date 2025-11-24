package de.hft.licensing.utils.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public int getExpiresIn() { return expiresIn; }
    public int getRefreshExpiresIn() { return refreshExpiresIn; }
    public String getTokenType() { return tokenType; }
}
