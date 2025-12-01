package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.UserMapperDTO;
import everoutproject.Event.domain.model.user.UserRepository;
import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.rest.dtos.user.UserDTO;
import everoutproject.Event.domain.model.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create user in database and return it as DTO
     */
    public UserDTO createUser(String email, String password, String firstName, String lastName){
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        UserRole userRole = UserRole.USER;
        LocalDate createdAt = LocalDate.now();

        //Hash password
        String hashpassword = passwordEncoder.encode(password);

        User newUser = new User(email, hashpassword, firstName, lastName, userRole);
        newUser.setCreatedAt(createdAt);

        userRepository.addNewUser(newUser);

        return UserMapperDTO.toDTO(newUser);

    }



    private String currentUserEmail = null;  // Use email instead of ID

    public void setCurrentUser(String email) {
        this.currentUserEmail = email;
    }

    public User getCurrentUser() {
        System.out.println("=== DEBUG getCurrentUser ===");
        System.out.println("currentUserEmail: " + currentUserEmail);

        if (currentUserEmail == null) {
            System.out.println("FALLBACK: Using first user from database");
            List<User> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) {
                throw new RuntimeException("No users found");
            }
            User firstUser = allUsers.get(0);
            System.out.println("First user email: " + firstUser.getEmail());
            return firstUser;
        }

        System.out.println("Using currentUserEmail: " + currentUserEmail);
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Log in user and return it as DTO
     */
    public UserDTO loginUser(String email, String password){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User with this email does not exist"));
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Incorrect password");
        }

        return UserMapperDTO.toDTO(user);
    }




    /**
     * Get all users as DTOs.
     */
    public List<UserDTO> getAllUserDTO() {
        return userRepository.findAll().stream()
                .map(UserMapperDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by email.
     */
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return UserMapperDTO.toDTO(user);
    }

    /**
     * Get user by ID.
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserMapperDTO.toDTO(user);
    }



    public User updateUserProfile(Long id, String firstName, String lastName, String newEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldEmail = user.getEmail(); // Alte Email speichern

        user.updateProfile(firstName, lastName);
        user.setEmail(newEmail);
        userRepository.save(user);

        // WICHTIG: currentUserEmail aktualisieren!
        if (currentUserEmail != null && currentUserEmail.equals(oldEmail)) {
            this.currentUserEmail = newEmail;
            System.out.println("=== Updated currentUserEmail to: " + newEmail);
        }

        return user;
    }

    /**
     * Update user password.
     */
    public void updateUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user profile.
     */
    public void updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole userRole = UserRole.valueOf(newRole.toUpperCase());
        user.updateRole(userRole);
        userRepository.save(user);
    }

    /**
     * Delete user.
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(id);
        if (currentUserEmail != null && currentUserEmail.equals(user.getEmail())) {
            this.currentUserEmail = null;
        }
    }

    /**
     * For debugging/logging purposes: prints all events to console.
     */
    @Transactional
    public void printUsers() {
        userRepository.findAll().forEach(user -> System.out.println(user.toString()));
    }
}
