package adventure.go.goadventure.user;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && userRepository.hasRole(userId, 1)) {
            return user;
        }
        return Optional.empty();
    }
}