package everoutproject.Event.domain.model.user;

import java.util.List;

public final class RoleRegistry {

    private static final Role ADMIN = new Role(1L, UserRole.ADMIN.name(), List.of(
            Permission.values()
    ));

    private static final Role BACKOFFICE = new Role(2L, UserRole.BACKOFFICE.name(), List.of(
            Permission.CREATE_EVENT,
            Permission.CANCEL_EVENT,
            Permission.EDIT_EVENT,
            Permission.CREATE_BOOKING,
            Permission.VIEW_ALL_BOOKINGS
    ));

    private static final Role FRONTOFFICE = new Role(3L, UserRole.FRONTOFFICE.name(), List.of(
            Permission.CREATE_BOOKING,
            Permission.VIEW_ALL_BOOKINGS
    ));

    private static final Role USER = new Role(4L, UserRole.USER.name(), List.of(
            Permission.CREATE_BOOKING,
            Permission.VIEW_OWN_BOOKINGS
    ));

    private static final Role GUEST = new Role(0L, "GUEST", List.of(
            Permission.CREATE_BOOKING
    ));

    private RoleRegistry() {}

    public static Role fromUserRole(UserRole role) {
        if (role == null) return GUEST;
        return switch (role) {
            case ADMIN -> ADMIN;
            case BACKOFFICE -> BACKOFFICE;
            case FRONTOFFICE -> FRONTOFFICE;
            case USER -> USER;
        };
    }

    public static Role guestRole() {
        return GUEST;
    }
}