package everoutproject.Event.domain.model.event;

import java.util.Optional;

public interface OrganizerRepository {
    Optional<Organizer> findById(Long id);
}
