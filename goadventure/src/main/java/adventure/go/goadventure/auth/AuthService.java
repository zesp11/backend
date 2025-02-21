package adventure.go.goadventure.auth;

import adventure.go.goadventure.dto.UserDTO;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.user.User;
import adventure.go.goadventure.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Set<String> invalidatedTokens = new HashSet<>();

    public AuthService(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    public AuthLoginResponse login(String login, String password) {
        Optional<User> userOptional = userService.findByLogin(login);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid login or password.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid login or password.");
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getId_user());
        String refreshToken = jwtUtil.generateRefreshToken(user.getLogin(), user.getId_user());
        UserDTO userDTO = new UserDTO(user.getId_user(), user.getLogin(), user.getEmail());

        return new AuthLoginResponse("Login successful.", token, refreshToken, userDTO);
    }

    public User register(String login, String email, String password) {
        Optional<User> existingUserByLogin = userService.findByLogin(login);
        Optional<User> existingUserByEmail = userService.findByEmail(email);

        if (existingUserByLogin.isPresent()) {
            throw new IllegalArgumentException("User with this login already exists.");
        }

        if (existingUserByEmail.isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(null, login, email, hashedPassword); // id_user set to null
        userService.save(newUser);
        return newUser;
    }

    public boolean authenticate(String login, String password) {
        Optional<User> userOptional = userService.findByLogin(login);

        if (userOptional.isEmpty()) {
            return false; // User with the given login does not exist
        }

        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void logout(String token) {
        if (invalidatedTokens.contains(token)) {
            throw new IllegalArgumentException("Token is already invalidated.");
        }
        invalidatedTokens.add(token);
    }

    public boolean isTokenValid(String token) {
        return !invalidatedTokens.contains(token);
    }

    public String refreshToken(String token) {
        if (isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);
            Integer userId = jwtUtil.getUserIdFromToken(token);
            return jwtUtil.generateToken(username, userId);
        } else {
            throw new IllegalArgumentException("Invalid or expired token.");
        }
    }
}