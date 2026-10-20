package com.Validation.payments.pojo;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String refreshToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
}