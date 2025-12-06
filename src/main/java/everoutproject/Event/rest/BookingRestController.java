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

    public static class CreateBookingRequest {
        @NotBlank(message = "First name is required")
        private String firstname;

        @NotBlank(message = "Last name is required")
        private String lastname;

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        private LocalDate birthdate;

        @NotBlank(message = "Street is required")
        private String street;

        @NotBlank(message = "House number is required")
        private String houseNumber;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotNull(message = "Event ID is required")
        private Long eventId;

        public String getFirstname() { return firstname; }
        public void setFirstname(String firstname) { this.firstname = firstname; }
        public String getLastname() { return lastname; }
        public void setLastname(String lastname) { this.lastname = lastname; }
        public LocalDate getBirthdate() { return birthdate; }
        public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getHouseNumber() { return houseNumber; }
        public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
    }

    public static class CreateBookingWithPaymentRequest extends CreateBookingRequest {
        @NotBlank(message = "Payment method is required")
        private String paymentMethod;

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
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
            @Valid @ModelAttribute CreateBookingRequest request,
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
            @Valid @ModelAttribute CreateBookingWithPaymentRequest request,
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
