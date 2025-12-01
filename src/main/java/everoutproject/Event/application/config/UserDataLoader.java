package everoutproject.Event.application.config;

import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.domain.model.user.UserRepository;
import everoutproject.Event.domain.model.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class UserDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createUserIfNotExists("admin@everout.at", "123456", "Admin", "Admin", UserRole.ADMIN);
        createUserIfNotExists("backoffice@everout.at", "123456", "Back", "Office", UserRole.BACKOFFICE);
        createUserIfNotExists("frontoffice@everout.at", "123456", "Front", "Office", UserRole.FRONTOFFICE);
        createUserIfNotExists("testuser@everout.at", "123456", "Test", "User", UserRole.USER);
    }

    private void createUserIfNotExists(String email, String rawPassword, String firstName, String lastName, UserRole role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(email, encodedPassword, firstName, lastName, role);
        userRepository.addNewUser(user);
    }
}