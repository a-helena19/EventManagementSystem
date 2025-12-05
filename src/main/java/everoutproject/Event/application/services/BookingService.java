package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.BookingMapperDTO;
import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventRepository;
import everoutproject.Event.domain.model.event.EventStatus;
import everoutproject.Event.rest.dtos.booking.BookingDTO;
import everoutproject.Event.rest.dtos.booking.RefundDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }


    public BookingDTO createBooking(String firstname, String lastname, LocalDate birthDate, BookingAddress address, String phoneNumber, String email, Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new RuntimeException("Event is not bookable");
        }

        if (event.getBookedParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is fully booked");
        }

        Booking newBooking = new Booking(firstname, lastname, birthDate, LocalDate.now(), address, phoneNumber, email, BookingStatus.ACTIVE, eventId, userId);

        // Persist event
        bookingRepository.addNewBooking(newBooking);

        event.increaseBookedParticipants(1);
        eventRepository.save(event);

        return BookingMapperDTO.toDTO(newBooking);
    }

    public List<BookingDTO> getAllBookingsDTO() {
        return bookingRepository.findAll().stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByEmail(String email) {
        return bookingRepository.findByEmail(email).stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
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
                throw new RuntimeException("Cancellation Deadline is expired");
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

        LocalDate startDate = event.getStartDate();
        Integer depositPercent = event.getDepositPercent();
        BigDecimal price = event.getPrice();
        BigDecimal refund = calculateRefund(startDate, depositPercent, price);

        return new RefundDTO(refund);

    }
}