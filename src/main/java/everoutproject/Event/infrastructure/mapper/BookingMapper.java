package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.domain.model.booking.BookingStatus;

public class BookingMapper {

    // Converts infrastructure entity to domain object
    public static Booking toDomain (everoutproject.Event.infrastructure.persistence.model.booking.Booking entity) {
        if (entity == null) return null;

        BookingAddress address = new BookingAddress (
                entity.getStreet(),
                entity.getHouseNumber(),
                entity.getCity(),
                entity.getPostalCode()
        );

        Booking domain = new Booking(
                entity.getFirstname(),
                entity.getLastname(),
                entity.getBirthDate(),
                entity.getBookingDate(),
                address,
                entity.getPhoneNumber(),
                entity.getEmail(),
                BookingStatus.valueOf(entity.getStatus().name()),
                entity.getEventId()
        );
        domain.setEventId(entity.getEventId());
        domain.setId(entity.getId());

        return domain;
    }

    // Converts domain to entity
    public static everoutproject.Event.infrastructure.persistence.model.booking.Booking toEntity(Booking domain) {
        if (domain == null) return null;

        everoutproject.Event.infrastructure.persistence.model.booking.Booking entity = new everoutproject.Event.infrastructure.persistence.model.booking.Booking(
                domain.getFirstname(),
                domain.getLastname(),
                domain.getBirthDate(),
                domain.getBookingDate(),
                domain.getAddress().getStreet(),
                domain.getAddress().getHouseNumber(),
                domain.getAddress().getCity(),
                domain.getAddress().getPostalCode(),
                domain.getPhoneNumber(),
                domain.getEmail(),
                everoutproject.Event.infrastructure.persistence.model.booking.BookingStatus.valueOf(domain.getStatus().name()),
                domain.getEventId()
        );
        entity.setId(domain.getId());
        return entity;
    }
}
