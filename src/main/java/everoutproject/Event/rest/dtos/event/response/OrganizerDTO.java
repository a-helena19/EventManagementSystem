package everoutproject.Event.rest.dtos.event.response;

public record OrganizerDTO(
        Long id,
        String name,
        String contactEmail,
        String phone
) {}
