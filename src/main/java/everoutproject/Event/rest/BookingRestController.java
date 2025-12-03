package everoutproject.Event.rest;

import everoutproject.Event.application.security.RoleChecker;
import everoutproject.Event.application.services.BookingService;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;
    private final RoleChecker roleChecker;

    public BookingRestController(BookingService bookingService, RoleChecker roleChecker) {
        this.bookingService = bookingService;
        this.roleChecker = roleChecker;
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long userId) {
        List<BookingDTO> bookings = bookingService.getFilteredBookings(email, userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getAccessibleBookings());
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam LocalDate birthdate,
            @RequestParam String street,
            @RequestParam String houseNumber,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Long eventId,
            Authentication authentication
    ) {

        Long userId = roleChecker.getUserId(authentication);

        BookingAddress address = new BookingAddress(street, houseNumber, city, postalCode);
        BookingDTO bookingDTO = bookingService.createBooking(
                firstname, lastname, birthdate, address, phone, email, userId, eventId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Booking created successfully",
                        "id", bookingDTO.id(),
                        "name", bookingDTO.firstname() + " " + bookingDTO.lastname()
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }
}
