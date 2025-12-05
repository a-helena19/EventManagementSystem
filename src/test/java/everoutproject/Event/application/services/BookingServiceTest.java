package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.BookingMapperDTO;
import everoutproject.Event.domain.model.booking.*;
import everoutproject.Event.application.services.BookingService;
import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventRepository;
import everoutproject.Event.domain.model.event.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private EventRepository eventRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        eventRepository = mock(EventRepository.class);
        bookingService = new BookingService(bookingRepository, eventRepository);
    }

    @Test
    void testCreateBooking() {

        // Arrange: booking address
        BookingAddress addr = new BookingAddress("Street", "10", "City", "12345");

        // Arrange: mock event that exists and is bookable
        Event event = mock(Event.class);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        when(event.getStatus()).thenReturn(EventStatus.ACTIVE);
        when(event.getBookedParticipants()).thenReturn(5);
        when(event.getMaxParticipants()).thenReturn(10);

        // we need this, because service calls event.increaseBookedParticipants
        doNothing().when(event).increaseBookedParticipants(anyInt());

        // service later calls eventRepository.save(event)
        doNothing().when(eventRepository).save(any(Event.class));

        // Arrange: booking saved to repo
        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(99L);
            return null;
        }).when(bookingRepository).addNewBooking(any());

        var dto = bookingService.createBooking(
                "Max", "Muster",
                LocalDate.of(1990, 1, 1),
                addr,
                "+4912345",
                "test@test.com",
                1L,
                2L
        );

        assertNotNull(dto);
        assertEquals(99L, dto.id());
        assertEquals("Max", dto.firstname());
    }

    @Test
    void testGetAllBookingsDTO() {
        BookingAddress addr = new BookingAddress("S", "1", "C", "1000");
        Booking b = new Booking("A", "B", LocalDate.now(), LocalDate.now(), addr, "123", "a@b.com", BookingStatus.ACTIVE, 1L, 2L);
        b.setId(10L);

        when(bookingRepository.findAll()).thenReturn(List.of(b));

        var list = bookingService.getAllBookingsDTO();

        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).id());
    }
}
