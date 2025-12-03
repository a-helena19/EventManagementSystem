package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.booking.*;
import everoutproject.Event.infrastructure.persistence.model.booking.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void testDomainToEntity() {
        Booking domain = new Booking(
                "Max", "Mustermann",
                LocalDate.of(2000,1,1),
                LocalDate.of(2024,1,1),
                new BookingAddress("Street", "10", "City", "12345"),
                "123", "mail@mail.com",
                everoutproject.Event.domain.model.booking.BookingStatus.ACTIVE,
                5L,
                6L
        );
        domain.setId(100L);

        var entity = BookingMapper.toEntity(domain);

        assertEquals(100L, entity.getId());
        assertEquals("Max", entity.getFirstname());
        assertEquals("10", entity.getHouseNumber());
    }

    @Test
    void testEntityToDomain() {
        var entity = new everoutproject.Event.infrastructure.persistence.model.booking.Booking();
        entity.setId(20L);
        entity.setFirstname("A");
        entity.setLastname("B");
        entity.setBirthDate(LocalDate.of(1999,1,1));
        entity.setBookingDate(LocalDate.of(2024,1,1));
        entity.setStreet("Str");
        entity.setHouseNumber("4");
        entity.setCity("C");
        entity.setPostalCode("99999");
        entity.setPhoneNumber("123");
        entity.setEmail("a@b.com");
        entity.setStatus(BookingStatus.ACTIVE);
        entity.setEventId(9L);
        entity.setUserId(5L);

        var domain = BookingMapper.toDomain(entity);

        assertEquals(20L, domain.getId());
        assertEquals("A", domain.getFirstname());
        assertEquals("C", domain.getAddress().getCity());
        assertEquals(5L, domain.getUserId());
    }
}
