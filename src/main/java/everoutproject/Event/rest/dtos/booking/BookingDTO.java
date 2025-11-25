package everoutproject.Event.rest.dtos.booking;

import java.time.LocalDate;

public record BookingDTO (
        Long id,
        String firstname,
        String lastname,
        LocalDate birthDate,
        LocalDate bookingDate,
        BookingAddressDTO address,
        String phoneNumber,
        String email,
        String status,
        Long eventId
) {}
