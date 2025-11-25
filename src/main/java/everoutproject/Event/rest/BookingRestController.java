package everoutproject.Event.rest;

import everoutproject.Event.application.services.BookingService;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsDTO());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(
            @RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam LocalDate birthdate,
            @RequestParam String street,
            @RequestParam String houseNumber,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Long eventId
    ) {
        try {
            BookingAddress address = new BookingAddress(street, houseNumber, city, postalCode);
            BookingDTO bookingDTO = bookingService.createBooking(firstname, lastname, birthdate, address, phone, email, eventId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Booking created successfully",
                            "id", bookingDTO.id(),
                            "name", bookingDTO.firstname() + " " + bookingDTO.lastname()
                    ));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create booking: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
