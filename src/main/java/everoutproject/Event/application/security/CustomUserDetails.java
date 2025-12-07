package everoutproject.Event.application.security;

import everoutproject.Event.domain.model.user.Role;
import everoutproject.Event.domain.model.user.RoleRegistry;
import everoutproject.Event.domain.model.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final UserRole userRole; // Das Enum aus der DB
    private final String firstName;
    private final String lastName;

    public CustomUserDetails(Long id, String email, String password, UserRole role, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userRole = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() { return id; }

    // Holt die mächtige Role-Klasse mit den Rechten
    public Role getRoleDefinition() {
        return RoleRegistry.fromUserRole(userRole);
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role roleDef = getRoleDefinition();

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.name()));

        authorities.addAll(roleDef.getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.name()))
                .collect(Collectors.toList()));

        return authorities;
    }

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return email; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}