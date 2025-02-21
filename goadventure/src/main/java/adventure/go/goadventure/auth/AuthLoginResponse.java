package adventure.go.goadventure.auth;

import adventure.go.goadventure.dto.UserDTO;

public class AuthLoginResponse {
    private String message;
    private String token;
    private String refreshToken;
    private UserDTO user;

    public AuthLoginResponse(String message, String token, String refreshToken, UserDTO user) {
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}