package at.fhv.Event.application.services;

import at.fhv.Event.application.dtos.BookingMapperDTO;
import at.fhv.Event.domain.model.booking.Booking;
import at.fhv.Event.domain.model.booking.BookingAddress;
import at.fhv.Event.domain.model.booking.BookingRepository;
import at.fhv.Event.domain.model.booking.BookingStatus;
import at.fhv.Event.rest.dtos.booking.BookingDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    public BookingDTO createBooking(String firstname, String lastname, LocalDate birthDate, BookingAddress address, String phoneNumber, String email, Long eventId) {

        Booking newBooking = new Booking(firstname, lastname, birthDate, LocalDate.now(), address, phoneNumber, email, BookingStatus.ACTIVE, eventId);

        // Persist event
        bookingRepository.addNewEvent(newBooking);

        return BookingMapperDTO.toDTO(newBooking);
    }

    public List<BookingDTO> getAllBookingsDTO() {
        return bookingRepository.findAll().stream()
                .map(BookingMapperDTO::toDTO)
                .collect(Collectors.toList());
    }
}
