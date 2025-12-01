package everoutproject.Event.rest;

import everoutproject.Event.application.services.UserService;
import everoutproject.Event.rest.dtos.user.UserDTO;
import everoutproject.Event.domain.model.user.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        if (password.length() < 6) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password must include at least 6 characters");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        try {
            UserDTO userDTO = userService.createUser(email, password, firstName, lastName);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "User created successfully",
                            "id", userDTO.id(),
                            "name", userDTO.firstName() + " " + userDTO.lastName()
                    ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create user: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password

    ) {
        try {
            UserDTO userDTO = userService.loginUser(email, password);
            userService.setCurrentUser(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "id", userDTO.id(),
                    "name", userDTO.firstName() + " " + userDTO.lastName()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().name(),
                    "createdAt", user.getCreatedAt(),
                    "updatedAt", user.getUpdatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email
    ) {
        try {
            // First get the current user to get their ID
            User currentUser = userService.getCurrentUser();
            Long userId = currentUser.getId();

            // Then update using that ID
            User user = userService.updateUserProfile(userId, firstName, lastName, email);

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().name(),
                    "createdAt", user.getCreatedAt(),
                    "updatedAt", user.getUpdatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update profile: " + e.getMessage()));
        }


    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile() {
        try {
            System.out.println("=== DELETE Profile Called ===");

            // Get current user first
            User currentUser = userService.getCurrentUser();
            System.out.println("Deleting user: " + currentUser.getEmail());

            Long userId = currentUser.getId();
            userService.deleteUser(userId);

            System.out.println("User deleted successfully");
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            System.out.println("Delete error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete account: " + e.getMessage()));
        }
    }






}
