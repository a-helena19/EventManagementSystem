package everoutproject.Event.rest.dtos.booking;

public record BookingAddressDTO(
        String street,
        String houseNumber,
        String city,
        String postalCode
) {
}
