package everoutproject.Event.application.security;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.user.Role;
import everoutproject.Event.domain.model.user.RoleRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("roleChecker")
public class RoleChecker {

    private final BookingRepository bookingRepository;

    public RoleChecker(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Role getRole(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getRoleDefinition();
        }
        return RoleRegistry.guestRole();
    }

    public Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }


    public boolean canCreateEvent(Authentication authentication) {
        return getRole(authentication).canCreateEvent();
    }

    public boolean canEditEvent(Authentication authentication) {
        return getRole(authentication).canEditEvent();
    }


    public boolean canCancelEvent(Authentication authentication) {
        return getRole(authentication).canCancelEvent();
    }


    public boolean canCreateBooking(Authentication authentication) {
        return getRole(authentication).canCreateBooking();
    }

    public boolean canViewAllBookings(Authentication authentication) {
        return getRole(authentication).canViewAllBookings();
    }


    public boolean canViewBooking(Authentication authentication, Long bookingId) {
        Role role = getRole(authentication);

        if (role.canViewAllBookings()) {
            return true;
        }

        if (role.canViewOwnBookings()) {
            Long currentUserId = getUserId(authentication);
            if (currentUserId == null) {
                return false;
            }

            List<Booking> userBookings = bookingRepository.findByUserId(currentUserId);
            return userBookings.stream()
                    .anyMatch(b -> b.getId().equals(bookingId));
        }
        
        return false;
    }


    public boolean canViewBookingsForUser(Authentication authentication, Long targetUserId) {
        Role role = getRole(authentication);

        if (role.canViewAllBookings()) {
            return true;
        }

        if (role.canViewOwnBookings()) {
            Long currentUserId = getUserId(authentication);
            return currentUserId != null && currentUserId.equals(targetUserId);
        }
        
        return false;
    }


    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null 
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails;
    }

    public boolean isGuest(Authentication authentication) {
        return !isAuthenticated(authentication);
    }
}
