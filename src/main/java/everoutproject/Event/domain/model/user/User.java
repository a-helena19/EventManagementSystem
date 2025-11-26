package everoutproject.Event.domain.model.user;

import java.time.LocalDateTime;

public class User {
    private final Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for new user (no ID yet)
    public User(String email,
                String password,
                String firstName,
                String lastName,
                UserRole role) {
        this(null, email, password, firstName, lastName, role, LocalDateTime.now(), LocalDateTime.now());
    }

    // Constructor for reconstruction (e.g. from persistence)
    public User(Long id,
                String email,
                String password,
                String firstName,
                String lastName,
                UserRole role,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Domain behavior
    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", firstName=" + firstName + 
               ", lastName=" + lastName + ", role=" + role + "]";
    }
}
