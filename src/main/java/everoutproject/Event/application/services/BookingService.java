package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.BookingMapperDTO;
import everoutproject.Event.application.security.RoleChecker;
import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventRepository;
import everoutproject.Event.domain.model.event.EventStatus;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import everoutproject.Event.rest.dtos.booking.RefundDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import everoutproject.Event.application.exceptions.PaymentFailedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoleChecker roleChecker;
    private final EventRepository eventRepository;
    private final PaymentService paymentService;

    public BookingService(BookingRepository bookingRepository, RoleChecker roleChecker,
                          EventRepository eventRepository, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.roleChecker = roleChecker;
        this.eventRepository = eventRepository;
        this.paymentService = paymentService;
    }


    @PreAuthorize("@roleChecker.canCreateBooking(authentication)")
    public BookingDTO createBooking(String firstname, String lastname, LocalDate birthDate,
                                    BookingAddress address, String phoneNumber, String email,
                                    Long userId, Long eventId) {


        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new RuntimeException("Event is not bookable");
        }

        if (event.getBookedParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is fully booked");
        }


        Booking newBooking = new Booking(
                firstname, lastname, birthDate, LocalDate.now(),
                address, phoneNumber, email,
                BookingStatus.ACTIVE, eventId, userId
        );

        // Persist event
        //bookingRepository.addNewBooking(newBooking);

        event.increaseBookedParticipants(1);
        eventRepository.save(event);

        bookingRepository.addNewBooking(newBooking);
        return BookingMapperDTO.toDTO(newBooking);
    }

    @PreAuthorize("@roleChecker.canCreateBooking(authentication)")
    public BookingDTO createBookingWithPayment(String firstname, String lastname, LocalDate birthDate,
                                               BookingAddress address, String phoneNumber, String email,
                                               Long userId, Long eventId, String paymentMethod)
            throws PaymentFailedException {

        // Load event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new RuntimeException("Event is not bookable");
        }

        if (event.getBookedParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is fully booked");
        }

        // Calculate deposit (default 30% if not set)
        Integer depositPercent = event.getDepositPercent() != null ? event.getDepositPercent() : 30;
        BigDecimal depositAmount = event.getPrice()
                .multiply(BigDecimal.valueOf(depositPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Process payment BEFORE creating booking
        paymentService.processPayment(depositAmount, paymentMethod);

        // Payment successful -> create booking
        Booking newBooking = new Booking(
                firstname, lastname, birthDate, LocalDate.now(),
                address, phoneNumber, email,
                BookingStatus.ACTIVE, eventId, userId
        );
        event.increaseBookedParticipants(1);
        eventRepository.save(event);

        bookingRepository.addNewBooking(newBooking);
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


public BigDecimal calculateRefund(LocalDate startDate, Integer depositPercent, BigDecimal price) {
    LocalDate freeCancelUntil = startDate.minusWeeks(4);   // 4 weeks before
    LocalDate percent25Until  = startDate.minusWeeks(2);   // 2 weeks before
    LocalDate percent70Until  = startDate.minusDays(3);  // 3 days before
    int refundPercent;

    LocalDate cancelDate = LocalDate.now();

    if (!cancelDate.isAfter(freeCancelUntil)) {
        refundPercent= 100;
    } else if (!cancelDate.isAfter(percent25Until)) {
        refundPercent= 75;
    } else if (!cancelDate.isAfter(percent70Until)) {
        refundPercent= 30;
    } else {
        refundPercent = 0;
    }

    BigDecimal deposit = price
            .multiply(BigDecimal.valueOf(depositPercent))
            .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);

    BigDecimal refund = deposit
            .multiply(BigDecimal.valueOf(refundPercent))
            .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);

    return refund;
}

public void cancelBooking(Long eventId, Long bookingId, String cancelReason) {
    Booking bookingToCancel = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

    LocalDate cancelDeadline = event.getCancelDeadline();
    LocalDate cancelDate = LocalDate.now();
    if (cancelDeadline != null) {
        if(cancelDeadline.isBefore(cancelDate)) {
            bookingToCancel.cancel(cancelReason, BigDecimal.ZERO);
            bookingRepository.save(bookingToCancel);
            return;
        }
    }
    LocalDate startDate = event.getStartDate();
    Integer depositPercent = event.getDepositPercent();
    BigDecimal price = event.getPrice();
    BigDecimal refund = calculateRefund(startDate, depositPercent, price);


        bookingToCancel.cancel(cancelReason, refund);
        bookingRepository.save(bookingToCancel);
    }
    public RefundDTO getRefund(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // refund = 0 when after cancellation deadline
        LocalDate cancelDeadline = event.getCancelDeadline();
        if (cancelDeadline != null) {
            if(cancelDeadline.isBefore(LocalDate.now())) {
                return new RefundDTO(BigDecimal.ZERO);
            }
        }

        LocalDate startDate = event.getStartDate();
        Integer depositPercent = event.getDepositPercent();
        BigDecimal price = event.getPrice();
        BigDecimal refund = calculateRefund(startDate, depositPercent, price);

        return new RefundDTO(refund);

    }
}