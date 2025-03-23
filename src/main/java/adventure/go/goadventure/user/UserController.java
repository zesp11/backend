package adventure.go.goadventure.user;

import adventure.go.goadventure.dto.PaginatedUserResponse;
import adventure.go.goadventure.dto.UserDTO;
import adventure.go.goadventure.dto.UserProfileUpdateDTO;
import adventure.go.goadventure.image.ImageUploadService;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;
    private  final ImageUploadService imageUploadService;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil, AuthService authService, UserService userService, ImageUploadService imageUploadService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.userService = userService;
        this.imageUploadService = imageUploadService;
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId_user(), user.getLogin(), user.getEmail(), (String) user.getBio(), user.getCreation_date(), user.getPhoto_url());
    }

    @GetMapping("")
    public PaginatedUserResponse findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        int offset = (page - 1) * limit;
        List<User> users;
        long total;

        if (search == null || search.isEmpty()) {
            users = userRepository.findAllWithPagination(offset, limit);
            total = userRepository.count();
        } else {
            users = userRepository.findByLoginOrEmailContaining(search);
            total = users.size();
        }

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found");
        }

        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PaginatedUserResponse(page, limit, total, userDTOs);
    }

    @GetMapping("/{id}")
    UserDTO findById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return convertToDTO(user.get());
    }

    @GetMapping("/login/{login}")
    UserDTO findByLogin(@PathVariable String login) {
        Optional<User> user = userRepository.findByLogin(login);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return convertToDTO(user.get());
    }

    @GetMapping("/email/{email}")
    UserDTO findByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return convertToDTO(user.get());
    }

    @GetMapping("/profile")
    public UserDTO getProfile(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return convertToDTO(user.get());
    }

//    @PostMapping("")
//    @ResponseStatus(HttpStatus.CREATED)
//    public UserDTO registerUser(@RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
//        try {
//            authService.register(userProfileUpdateDTO.getLogin(), userProfileUpdateDTO.getEmail(), userProfileUpdateDTO.getPassword());
//            Optional<User> userOptional = userService.findByLogin(userProfileUpdateDTO.getLogin());
//            if (userOptional.isEmpty()) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found after registration");
//            }
//            return convertToDTO(userOptional.get());
//        } catch (IllegalArgumentException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    void update(@RequestBody User user, @PathVariable Integer id) {
        userRepository.update(user, id);
    }

    @PutMapping("/profile")
    public UserDTO updateProfile(@RequestHeader("Authorization") String token, @ModelAttribute UserProfileUpdateDTO userProfileUpdateDTO) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        if (userProfileUpdateDTO.getLogin() == null || userProfileUpdateDTO.getLogin().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login is required.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOptional.get();
        user.setLogin(userProfileUpdateDTO.getLogin());
        user.setEmail(userProfileUpdateDTO.getEmail());
        user.setBio(userProfileUpdateDTO.getBio());
        if (userProfileUpdateDTO.getPassword() != null && !userProfileUpdateDTO.getPassword().isEmpty()) {
            user.setPassword(new BCryptPasswordEncoder().encode(userProfileUpdateDTO.getPassword()));
        }

        MultipartFile photo = userProfileUpdateDTO.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = imageUploadService.uploadImage(photo);
                user.setPhoto_url(photoUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
            }
        }

        userRepository.update(user, userId);

        return convertToDTO(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> deleteUser(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(jwtToken);
        Optional<User> userOptional = userRepository.findById(userIdFromToken);

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User userFromToken = userOptional.get();
        if (!userFromToken.getId_user().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions to delete user");
        }

        userRepository.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return response;
    }
}