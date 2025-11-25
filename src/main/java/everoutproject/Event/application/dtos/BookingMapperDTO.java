package everoutproject.Event.application.dtos;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingAddress;
import everoutproject.Event.rest.dtos.booking.BookingAddressDTO;
import everoutproject.Event.rest.dtos.booking.BookingDTO;

public class BookingMapperDTO {

    public static BookingDTO toDTO(Booking booking){
        return new BookingDTO(
                booking.getId(),
                booking.getFirstname(),
                booking.getLastname(),
                booking.getBirthDate(),
                booking.getBookingDate(),
                addressToDTO(booking.getAddress()),
                booking.getPhoneNumber(),
                booking.getEmail(),
                booking.getStatus().name(),
                booking.getEventId()
        );
    }

    public static BookingAddressDTO addressToDTO(BookingAddress address) {
        if (address == null) return null;
        return new BookingAddressDTO(
                address.getStreet(),
                address.getHouseNumber(),
                address.getCity(),
                address.getPostalCode()
        );
    }
}
