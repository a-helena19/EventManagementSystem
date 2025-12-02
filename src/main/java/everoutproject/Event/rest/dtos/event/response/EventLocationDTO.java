package everoutproject.Event.rest.dtos.event.response;

public record EventLocationDTO(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        String state,
        String country
) {}
