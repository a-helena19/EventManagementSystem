package at.fhv.Event.infrastructure.mapper;

import at.fhv.Event.domain.model.booking.Booking;
import at.fhv.Event.domain.model.booking.BookingAddress;
import at.fhv.Event.domain.model.booking.BookingStatus;

public class BookingMapper {

    // Converts infrastructure entity to domain object
    public static Booking toDomain (at.fhv.Event.infrastructure.persistence.model.booking.Booking entity) {
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
                BookingStatus.valueOf(entity.getStatus().name())
        );
        domain.setEventId(entity.getEventId());

        return domain;
    }

    // Converts domain to entity
    public static at.fhv.Event.infrastructure.persistence.model.booking.Booking toEntity(Booking domain) {
        if (domain == null) return null;

        at.fhv.Event.infrastructure.persistence.model.booking.Booking entity = new at.fhv.Event.infrastructure.persistence.model.booking.Booking(
                domain.getLastname(),
                domain.getLastname(),
                domain.getBirthDate(),
                domain.getBookingDate(),
                domain.getAddress().getStreet(),
                domain.getAddress().getHouseNumber(),
                domain.getAddress().getCity(),
                domain.getAddress().getPostalCode(),
                domain.getPhoneNumber(),
                domain.getEmail(),
                at.fhv.Event.infrastructure.persistence.model.booking.BookingStatus.valueOf(domain.getStatus().name())
        );

        entity.setEventId(domain.getEventId());

        return entity;
    }
}
