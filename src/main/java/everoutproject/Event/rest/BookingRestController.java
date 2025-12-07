package everoutproject.Event.rest;

import everoutproject.Event.application.security.RoleChecker;
import everoutproject.Event.application.services.BookingService;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import everoutproject.Event.rest.dtos.booking.CancelBookingRequestDTO;
import everoutproject.Event.rest.dtos.booking.RefundDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import everoutproject.Event.application.exceptions.PaymentFailedException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import everoutproject.Event.rest.dtos.booking.CreateBookingRequestDTO;
import everoutproject.Event.rest.dtos.booking.CreateBookingWithPaymentRequestDTO;

import java.time.LocalDate;
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
            @Valid @ModelAttribute CreateBookingRequestDTO request,
            Authentication authentication
    ) {
        Long userId = roleChecker.getUserId(authentication);

        BookingAddress address = new BookingAddress(request.getStreet(), request.getHouseNumber(), request.getCity(), request.getPostalCode());
        BookingDTO bookingDTO = bookingService.createBooking(
                request.getFirstname(), request.getLastname(), request.getBirthdate(), address, request.getPhone(), request.getEmail(), userId, request.getEventId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Booking created successfully",
                        "id", bookingDTO.id(),
                        "name", bookingDTO.firstname() + " " + bookingDTO.lastname()
                ));
    }

    @PostMapping("/createWithPayment")
    public ResponseEntity<Map<String, Object>> createBookingWithPayment(
            @Valid @ModelAttribute CreateBookingWithPaymentRequestDTO request,
            Authentication authentication
    ) {
        try {
            Long userId = roleChecker.getUserId(authentication);

            BookingAddress address = new BookingAddress(request.getStreet(), request.getHouseNumber(), request.getCity(), request.getPostalCode());
            BookingDTO bookingDTO = bookingService.createBookingWithPayment(
                    request.getFirstname(), request.getLastname(), request.getBirthdate(), address, request.getPhone(), request.getEmail(), userId, request.getEventId(), request.getPaymentMethod()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Booking created successfully",
                            "id", bookingDTO.id(),
                            "firstname", bookingDTO.firstname(),
                            "lastname", bookingDTO.lastname(),
                            "email", bookingDTO.email()
                    ));
        } catch (PaymentFailedException e) {
            return ResponseEntity
                    .status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }

    // Cancel a booking
    @PutMapping("/cancel/{eventId}/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long eventId, @PathVariable Long bookingId, @RequestBody CancelBookingRequestDTO request) {
        try {
            bookingService.cancelBooking(eventId, bookingId, request.getReason());
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to cancel booking: " + e.getMessage()));
        }
    }

    @GetMapping("/refund/{id}")
    public ResponseEntity<RefundDTO> getRefund(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getRefund(id));
    }
}
