package everoutproject.Event.application.dtos;

import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.rest.dtos.event.response.OrganizerDTO;

public class OrganizerMapperDTO {
    public static OrganizerDTO toDTO(Organizer domain) {
        return new OrganizerDTO(
                domain.getId(),
                domain.getName(),
                domain.getContactEmail(),
                domain.getPhone()
        );
    }
}
