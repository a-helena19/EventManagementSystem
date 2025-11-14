package at.fhv.Event.rest.dtos.event;

public record EventLocationDTO(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        String state,
        String country
) {}
