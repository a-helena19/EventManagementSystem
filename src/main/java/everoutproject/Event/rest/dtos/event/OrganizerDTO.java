package everoutproject.Event.rest.dtos.event;

public record OrganizerDTO(
        Long id,
        String name,
        String contactEmail,
        String phone
) {}
