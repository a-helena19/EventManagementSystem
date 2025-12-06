package everoutproject.Event.rest;

import everoutproject.Event.application.security.CustomUserDetails;
import everoutproject.Event.application.services.UserService;
import everoutproject.Event.domain.model.user.Role;
import everoutproject.Event.application.dtos.UserMapperDTO;
import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.rest.dtos.user.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserRestController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    public static class CreateUserRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateProfileRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @ModelAttribute CreateUserRequest request) {
        try {
            UserDTO userDTO = userService.createUser(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "User created successfully",
                            "id", userDTO.id(),
                            "name", userDTO.firstName() + " " + userDTO.lastName(),
                            "email", userDTO.email()
                    ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create user: " +
                    (e.getMessage() != null ? e.getMessage() : e.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @ModelAttribute LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            Role role = principal.getRoleDefinition();

            // IMPORTANT: Set the current user in the service
            userService.setCurrentUser(loginRequest.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "id", principal.getId(),
                    "name", principal.getFullName().isBlank() ? principal.getUsername() : principal.getFullName(),
                    "role", role.getRoleName()
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login failed: " +
                    (e.getMessage() != null ? e.getMessage() : "Invalid credentials"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            userService.logoutUser();
            return ResponseEntity.ok(Map.of(
                    "message", "Logout successful"
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-login")
    public ResponseEntity<?> checkLogin() {
        try {
            boolean isLoggedIn = userService.isLoggedIn();
            return ResponseEntity.ok(Map.of(
                    "isLoggedIn", isLoggedIn
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "isLoggedIn", false
            ));
        }
    }


    // NEW ENDPOINT: Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            UserDTO userDTO = UserMapperDTO.toDTO(currentUser);

            return ResponseEntity.ok(Map.of(
                    "id", userDTO.id(),
                    "email", userDTO.email(),
                    "firstName", userDTO.firstName(),
                    "lastName", userDTO.lastName(),
                    "role", userDTO.role()
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to get user profile: " +
                    (e.getMessage() != null ? e.getMessage() : "User not logged in"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // NEW ENDPOINT: Update user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @ModelAttribute UpdateProfileRequest request) {
        try {
            User currentUser = userService.getCurrentUser();

            User updatedUser = userService.updateUserProfile(
                    currentUser.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail()
            );

            UserDTO userDTO = UserMapperDTO.toDTO(updatedUser);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "id", userDTO.id(),
                    "email", userDTO.email(),
                    "firstName", userDTO.firstName(),
                    "lastName", userDTO.lastName(),
                    "role", userDTO.role()
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update profile: " +
                    (e.getMessage() != null ? e.getMessage() : "Update failed"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // NEW ENDPOINT: Delete current user
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteCurrentUser() {
        try {
            User currentUser = userService.getCurrentUser();
            userService.deleteUser(currentUser.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Account deleted successfully"
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


@GetMapping
public ResponseEntity<?> getAllUsers() {
    try {
        return ResponseEntity.ok(userService.getAllUserDTO());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
    }
}

@PutMapping("/{id}/role")
public ResponseEntity<?> updateUserRole(
        @PathVariable Long id,
        @RequestParam String role
) {
    try {
        String normalizedRole = role.toUpperCase();
        userService.updateUserRole(id, normalizedRole);
        return ResponseEntity.ok(Map.of(
                "message", "User role updated successfully",
                "id", id,
                "role", normalizedRole
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid role provided"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
    }
}

}