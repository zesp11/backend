package adventure.go.goadventure.auth;

import adventure.go.goadventure.dto.UserDTO;
import adventure.go.goadventure.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRegisterResponse> register(@RequestBody AuthRequest authRequest) {
        try {
            User newUser = authService.register(authRequest.getLogin(), authRequest.getEmail(), authRequest.getPassword());
            UserDTO userDTO = new UserDTO(newUser.getId_user(), newUser.getLogin(), newUser.getEmail());
            AuthRegisterResponse response = new AuthRegisterResponse("User registered successfully.", userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthRegisterResponse(ex.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            AuthLoginResponse loginResponse = authService.login(authRequest.getLogin(), authRequest.getPassword());
            return ResponseEntity.ok(loginResponse);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthErrorResponse("Invalid credentials."));
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or missing refresh token.");
        }

        String jwtToken = token.substring(7);
        authService.logout(jwtToken);

        return Map.of("message", "Logout successful.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        try {
            token = token.replace("Bearer ", "").trim();
            String newToken = authService.refreshToken(token);
            return ResponseEntity.ok(new AuthResponse(newToken));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }
}
