package adventure.go.goadventure.auth;

public class AuthErrorResponse {
    private String error;

    public AuthErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}