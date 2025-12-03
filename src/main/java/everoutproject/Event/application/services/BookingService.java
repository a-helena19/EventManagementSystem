package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.BookingMapperDTO;
import everoutproject.Event.application.security.RoleChecker;
import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoleChecker roleChecker;

    public BookingService(BookingRepository bookingRepository, RoleChecker roleChecker) {
        this.bookingRepository = bookingRepository;
        this.roleChecker = roleChecker;
    }


    @PreAuthorize("@roleChecker.canCreateBooking(authentication)")
    public BookingDTO createBooking(String firstname, String lastname, LocalDate birthDate,
                                    BookingAddress address, String phoneNumber, String email,
                                    Long userId, Long eventId) {

        Booking newBooking = new Booking(
                firstname, lastname, birthDate, LocalDate.now(),
                address, phoneNumber, email,
                BookingStatus.ACTIVE, eventId, userId
        );

        bookingRepository.addNewEvent(newBooking);
        return BookingMapperDTO.toDTO(newBooking);
    }

    @PreAuthorize("@roleChecker.canViewAllBookings(authentication)")
    public List<BookingDTO> getAllBookingsDTO() {
        return bookingRepository.findAll().stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }


    @PreAuthorize("@roleChecker.canViewAllBookings(authentication) or " +
            "(#email == authentication.name)")
    public List<BookingDTO> getBookingsByEmail(String email) {
        return bookingRepository.findByEmail(email).stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }


    @PreAuthorize("@roleChecker.canViewBookingsForUser(authentication, #userId)")
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAccessibleBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (roleChecker.canViewAllBookings(auth)) {
            return getAllBookingsDTO();
        }

        if (roleChecker.isAuthenticated(auth)) {
            Long userId = roleChecker.getUserId(auth);
            if (userId != null) {
                return getBookingsByUserIdInternal(userId);
            }
        }

        return List.of();
    }


    private List<BookingDTO> getBookingsByUserIdInternal(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }


    @PreAuthorize("@roleChecker.canViewAllBookings(authentication) or " +
            "@roleChecker.canViewBooking(authentication, #bookingId)")
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findAll().stream()
                .filter(b -> b.getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }


    public List<BookingDTO> getFilteredBookings(String filterEmail, Long filterUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (roleChecker.canViewAllBookings(auth)) {
            if (filterUserId != null) {
                return getBookingsByUserId(filterUserId);
            }
            if (filterEmail != null && !filterEmail.isBlank()) {
                return getBookingsByEmail(filterEmail);
            }
            return getAllBookingsDTO();
        }

        Long currentUserId = roleChecker.getUserId(auth);
        if (currentUserId != null) {
            return bookingRepository.findByUserId(currentUserId).stream()
                    .map(BookingMapperDTO::toDTO)
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
