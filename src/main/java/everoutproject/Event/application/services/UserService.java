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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import everoutproject.Event.infrastructure.persistence.model.user.UserJPARepository;

@Service
public class UserService {

    private static final String SESSION_USER_EMAIL = "userEmail";
    //private final HttpSession httpSession;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private String currentUserEmail = null;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, HttpSession httpSession){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Add this helper method
    private HttpSession getSession() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            return attr.getRequest().getSession(true);
        }
        catch (IllegalStateException e)
        {
            System.out.println("=== DEBUG: No HTTP request context available");
            return null;
        }
        }



    public UserDTO createUser(String email, String password, String firstName, String lastName){
        System.out.println("DEBUG: Creating user with email: " + email);
        if (userRepository == null) System.out.println("DEBUG: userRepository is NULL");
        if (passwordEncoder == null) System.out.println("DEBUG: passwordEncoder is NULL");

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


    public void setCurrentUser(String email) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(SESSION_USER_EMAIL, email);
            System.out.println("=== DEBUG: Current user set to: " + email);
        }
    }



    public User getCurrentUser() {
        HttpSession session = getSession();
        String email = null;

        if (session != null) {
            email = (String) session.getAttribute(SESSION_USER_EMAIL);
        }

        if (email == null) {
            throw new RuntimeException("No user logged in");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Log in user and return it as DTO
     */
        public UserDTO loginUser(String email, String password) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User with this email does not exist"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Incorrect password");
            }

            setCurrentUser(email); // This sets the session

            return UserMapperDTO.toDTO(user);
        }

    public void logoutUser() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(SESSION_USER_EMAIL);
        }
    }

    public boolean isLoggedIn() {
        HttpSession session = getSession();
        if (session == null) {
            return false;
        }
        return session.getAttribute(SESSION_USER_EMAIL) != null;
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
        User user = userRepository.findById(id ).orElseThrow(() -> new RuntimeException("User not found"));

        String oldEmail = user.getEmail();
        user.updateProfile(firstName, lastName);
        user.setEmail(newEmail);
        userRepository.save(user);

        HttpSession session = getSession();
        if (session != null) {
            String sessionEmail = (String) session.getAttribute(SESSION_USER_EMAIL);
            if (sessionEmail != null && sessionEmail.equals(oldEmail)) {
                session.setAttribute(SESSION_USER_EMAIL, newEmail);
            }
        }

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

        HttpSession session = getSession();
        if (session != null) {
            String sessionEmail = (String) session.getAttribute(SESSION_USER_EMAIL);
            if (sessionEmail != null && sessionEmail.equals(user.getEmail())) {
                session.removeAttribute(SESSION_USER_EMAIL);
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
