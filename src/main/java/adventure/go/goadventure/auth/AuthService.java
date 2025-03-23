package adventure.go.goadventure.auth;

import adventure.go.goadventure.dto.UserDTO;
import adventure.go.goadventure.dto.UserLoginDTO;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.user.User;
import adventure.go.goadventure.user.UserService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Set<String> invalidatedTokens = new HashSet<>();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AuthService(UserService userService, JwtUtil jwtUtil, NamedParameterJdbcTemplate jdbcTemplate) {
        this.userService = userService;
        this.jdbcTemplate = jdbcTemplate;
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
        UserLoginDTO userLoginDTO = new UserLoginDTO(user.getId_user(), user.getLogin(), user.getEmail(), (String) user.getBio(), user.getCreation_date(), user.getPhoto_url(), user.getRole());

        return new AuthLoginResponse("Login successful.", token, refreshToken, userLoginDTO);
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
        User newUser = new User(null, login, email, hashedPassword, null, LocalDateTime.now(), null); // id_user set to null
        userService.save(newUser);

        String sql = "INSERT INTO public.\"User_Role\" (id_user, id_role) VALUES (:id_user, :id_role)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_user", newUser.getId_user())
                .addValue("id_role", 3);
        jdbcTemplate.update(sql, params);

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
        return jwtUtil.validateToken(token) && jwtUtil.validateTokenStructure(token) && !invalidatedTokens.contains(token);
    }

    public boolean isAdmin(Integer userId) {
        Optional<User> userOptional = userService.findById(userId);
        return userOptional.isPresent();
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