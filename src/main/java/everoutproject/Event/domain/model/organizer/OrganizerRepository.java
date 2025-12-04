package everoutproject.Event.domain.model.organizer;

import java.util.List;
import java.util.Optional;

public interface OrganizerRepository {
    Optional<Organizer> findById(Long id);
    List<Organizer> findAll();
    Organizer save(Organizer domainOrg);

}
