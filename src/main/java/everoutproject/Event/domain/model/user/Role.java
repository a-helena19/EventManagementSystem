package everoutproject.Event.domain.model.user;

import java.util.List;

public class Role {
    private final Long id;
    private final String roleName;
    private final List<Permission> permissions;

    public Role(Long id, String roleName, List<Permission> permissions) {
        this.id = id;
        this.roleName = roleName;
        this.permissions = List.copyOf(permissions);
    }

    public Long getId() { return id; }
    public String getRoleName() { return roleName; }
    public List<Permission> getPermissions() { return permissions; }

    // --- Convenience Checks ---

    public boolean canCreateEvent() {
        return permissions.contains(Permission.CREATE_EVENT);
    }

    public boolean canCancelEvent() {
        return permissions.contains(Permission.CANCEL_EVENT);
    }

    public boolean canEditEvent() {
        return permissions.contains(Permission.EDIT_EVENT);
    }

    public boolean canCreateBooking() {
        return permissions.contains(Permission.CREATE_BOOKING);
    }

    public boolean canViewAllBookings() {
        return permissions.contains(Permission.VIEW_ALL_BOOKINGS);
    }

    public boolean canViewOwnBookings() {
        return permissions.contains(Permission.VIEW_OWN_BOOKINGS);
    }

    public boolean canAccessAdminConsole() {
        return permissions.contains(Permission.ACCESS_ADMIN_CONSOLE);
    }
}