package adventure.go.goadventure.auth;

import adventure.go.goadventure.dto.UserDTO;
import adventure.go.goadventure.dto.UserLoginDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthLoginResponse {
    private String message;
    private String token;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private UserLoginDTO user;


    public AuthLoginResponse(String message, String token, String refreshToken, UserLoginDTO user) {
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

    public UserLoginDTO getUser() {
        return user;
    }

    public void setUser(UserLoginDTO user) {
        this.user = user;
    }

}