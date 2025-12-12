package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.UserMapperDTO;
import everoutproject.Event.application.security.CustomUserDetails;
import everoutproject.Event.domain.model.user.UserRepository;
import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.rest.dtos.user.UserDTO;
import everoutproject.Event.domain.model.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, HttpSession httpSession){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

        userRepository.save(newUser);
        System.out.println("DEBUG ID = " + newUser.getId());

        return UserMapperDTO.toDTO(newUser);
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
     * Get user dto by email.
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
        User user = userRepository.findById(id ).orElseThrow(() -> new RuntimeException("User not found"));

        user.updateProfile(firstName, lastName);
        user.setEmail(newEmail);
        userRepository.save(user);

        return user;
    }

    /**
     * Update user password.
     */
    public void updateUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Hash the new password before saving
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(hashedPassword);
        userRepository.save(user);
    }

    /**
     * Update user profile.
     */
    public void updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole userRole = UserRole.valueOf(newRole.toUpperCase());

        // Check if the logged-in user is trying to remove their own admin rights
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

            //If the current user is the same as the target user and the new role is not ADMIN, throw exception
            if (currentUser.getId().equals(id) && userRole != UserRole.ADMIN) {
                throw new RuntimeException("Admins cannot remove their own admin rights");
            }
        }


        user.updateRole(userRole);
        userRepository.save(user);
    }

    /**
     * Delete user.
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

            if (currentUser.getId().equals(id) && user.getRole() == UserRole.ADMIN) {
                throw new RuntimeException("Admins cannot delete their own account");
            }
        }

        userRepository.delete(id);
    }

    /**
     * For debugging/logging purposes: prints all events to console.
     */
    @Transactional
    public void printUsers() {
        userRepository.findAll().forEach(user -> System.out.println(user.toString()));
    }
}
