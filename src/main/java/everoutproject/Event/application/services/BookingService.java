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
import org.springframework.stereotype.Service;

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
}
