package everoutproject.Event.rest;

import everoutproject.Event.application.security.CustomUserDetails;
import everoutproject.Event.application.services.UserService;
import everoutproject.Event.domain.model.user.Role;
import everoutproject.Event.application.dtos.UserMapperDTO;
import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.rest.dtos.user.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserRestController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            UserDTO userDTO = userService.createUser(email, password, firstName, lastName);

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
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            Role role = principal.getRoleDefinition();

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "id", principal.getId(),
                    "email", principal.getUsername(),
                    "name", principal.getFullName().isBlank() ? principal.getUsername() : principal.getFullName(),
                    "role", normalizeRole(role.getRoleName()),
                    "isLoggedIn", true
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login failed: " +
                    (e.getMessage() != null ? e.getMessage() : "Invalid credentials"));
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();   // delete session
            }

            SecurityContextHolder.clearContext(); //delete security

            return ResponseEntity.ok(Map.of(
                    "message", "Logout successful",
                    "isLoggedIn", false
            ));

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.ok(Map.of("role", "GUEST", "isLoggedIn", false));
        }

        SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (context == null || context.getAuthentication() == null) {
            return ResponseEntity.ok(Map.of("role", "GUEST", "isLoggedIn", false));
        }

        Authentication authentication = context.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.ok(Map.of("role", "GUEST", "isLoggedIn", false));
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getUsername(),
                "fullName", user.getFullName(),
                "role", normalizeRole(user.getRoleDefinition().getRoleName()),
                "isLoggedIn", true
        ));
    }




    // NEW ENDPOINT: Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(HttpServletRequest request, Authentication authentication) {
        try {
            CustomUserDetails principal = resolvePrincipal(request, authentication);
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
            }

            UserDTO userDTO = userService.getUserByEmail(principal.getUsername());

            return ResponseEntity.ok(Map.of(
                    "id", userDTO.id(),
                    "email", userDTO.email(),
                    "firstName", userDTO.firstName(),
                    "lastName", userDTO.lastName(),
                    "role", normalizeRole(userDTO.role())
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
    public ResponseEntity<?> updateUserProfile(
            HttpServletRequest request,
            Authentication authentication,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email) {

        try {
            CustomUserDetails principal = resolvePrincipal(request, authentication);
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
            }

            UserDTO currentUser = userService.getUserByEmail(principal.getUsername());

            User updatedUser = userService.updateUserProfile(
                    currentUser.id(),
                    firstName,
                    lastName,
                    email
            );

            UserDTO userDTO = UserMapperDTO.toDTO(updatedUser);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "id", userDTO.id(),
                    "email", userDTO.email(),
                    "firstName", userDTO.firstName(),
                    "lastName", userDTO.lastName(),
                    "role", normalizeRole(userDTO.role())
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
    public ResponseEntity<?> deleteCurrentUser(HttpServletRequest request, Authentication authentication) {
        try {
            CustomUserDetails principal = resolvePrincipal(request, authentication);;
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
            }
            userService.deleteUser(principal.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Account deleted successfully"
            ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public ResponseEntity<?> getAllUsers() {
    try {
        return ResponseEntity.ok(userService.getAllUserDTO());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
    }
}

@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}/role")
public ResponseEntity<?> updateUserRole(
        @PathVariable Long id,
        @RequestParam String role
) {
    try {
        String normalizedRole = normalizeRole(role);
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
    private CustomUserDetails resolvePrincipal(HttpServletRequest request, Authentication authentication) {
        // prefer the injected Authentication if present and valid
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        // fallback to session-stored SecurityContext (works for cookie/session auth)
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object ctxObj = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (!(ctxObj instanceof SecurityContext)) return null;
        SecurityContext ctx = (SecurityContext) ctxObj;
        Authentication auth = ctx.getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;
        return (CustomUserDetails) auth.getPrincipal();
    }

    private static String normalizeRole(String raw) {
        if (raw == null) return "GUEST";
        // remove optional "ROLE_" prefix and uppercase
        String r = raw.toUpperCase();
        if (r.startsWith("ROLE_")) r = r.substring(5);
        return r;
    }

}