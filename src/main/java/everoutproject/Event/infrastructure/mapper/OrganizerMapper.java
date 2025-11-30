package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.event.Organizer;

public class OrganizerMapper {

    public static Organizer toDomain(
            everoutproject.Event.infrastructure.persistence.model.event.Organizer entity) {

        return new Organizer(
                entity.getId(),
                entity.getName(),
                entity.getContactEmail(),
                entity.getPhone()
        );
    }

    public static everoutproject.Event.infrastructure.persistence.model.event.Organizer toEntity(
            Organizer domain) {

        return new everoutproject.Event.infrastructure.persistence.model.event.Organizer(
                domain.getId(),
                domain.getName(),
                domain.getContactEmail(),
                domain.getPhone()
        );
    }

}
