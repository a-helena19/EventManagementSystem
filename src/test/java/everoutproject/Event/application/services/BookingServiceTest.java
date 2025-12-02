package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.BookingMapperDTO;
import everoutproject.Event.domain.model.booking.*;
import everoutproject.Event.application.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingService = new BookingService(bookingRepository);
    }

    @Test
    void testCreateBooking() {
        BookingAddress addr = new BookingAddress("Street", "10", "City", "12345");

        Booking savedBooking = new Booking(
                "Max", "Muster",
                LocalDate.of(1990, 1, 1),
                LocalDate.now(),
                addr,
                "+4912345",
                "test@test.com",
                BookingStatus.ACTIVE,
                1L
        );
        savedBooking.setId(99L);

        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(99L);
            return null;
        }).when(bookingRepository).addNewEvent(any());

        var dto = bookingService.createBooking(
                "Max", "Muster",
                LocalDate.of(1990, 1, 1),
                addr,
                "+4912345",
                "test@test.com",
                1L
        );

        assertNotNull(dto);
        assertEquals(99L, dto.id());
        assertEquals("Max", dto.firstname());
    }

    @Test
    void testGetAllBookingsDTO() {
        BookingAddress addr = new BookingAddress("S", "1", "C", "1000");
        Booking b = new Booking("A", "B", LocalDate.now(), LocalDate.now(), addr, "123", "a@b.com", BookingStatus.ACTIVE, 1L);
        b.setId(10L);

        when(bookingRepository.findAll()).thenReturn(List.of(b));

        var list = bookingService.getAllBookingsDTO();

        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).id());
    }
}
