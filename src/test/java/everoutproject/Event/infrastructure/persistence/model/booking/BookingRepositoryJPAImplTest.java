package everoutproject.Event.infrastructure.persistence.model.booking;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.domain.model.booking.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingRepositoryJPAImplTest {

    private BookingJPARepository jpaRepository;
    private BookingRepositoryJPAImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(BookingJPARepository.class);
        repository = new BookingRepositoryJPAImpl(jpaRepository);
    }

    @Test
    void testAddNewBooking() {
        Booking domain = new Booking(
                "A", "B",
                LocalDate.now(), LocalDate.now(),
                new BookingAddress("S", "1", "C", "1000"),
                "123", "mail@mail.com",
                BookingStatus.ACTIVE,
                1L,
                2L  //user id
        );

        everoutproject.Event.infrastructure.persistence.model.booking.Booking savedEntity =
                new everoutproject.Event.infrastructure.persistence.model.booking.Booking();
        savedEntity.setId(55L);

        when(jpaRepository.save(any())).thenReturn(savedEntity);

        repository.addNewEvent(domain);

        assertEquals(55L, domain.getId());
    }

    @Test
    void testFindAll() {
        everoutproject.Event.infrastructure.persistence.model.booking.Booking entity =
                new everoutproject.Event.infrastructure.persistence.model.booking.Booking();

        entity.setId(10L);
        entity.setFirstname("X");
        entity.setLastname("Y");
        entity.setBirthDate(LocalDate.now());
        entity.setBookingDate(LocalDate.now());
        entity.setStreet("S");
        entity.setHouseNumber("1");
        entity.setCity("C");
        entity.setPostalCode("1000");
        entity.setPhoneNumber("12345");
        entity.setEmail("x@y.com");
        entity.setStatus(everoutproject.Event.infrastructure.persistence.model.booking.BookingStatus.ACTIVE);  // 🔥 REQUIRED
        entity.setEventId(99L);

        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        var result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("X", result.get(0).getFirstname());
    }

}

