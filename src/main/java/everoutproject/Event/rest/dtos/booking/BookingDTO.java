package everoutproject.Event.rest.dtos.booking;

import java.math.BigDecimal;
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
        LocalDate cancelDate,
        String cancelReason,
        BigDecimal refund,
        Long eventId
) {}
