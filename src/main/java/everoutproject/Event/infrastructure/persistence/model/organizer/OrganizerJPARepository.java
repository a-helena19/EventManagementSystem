package everoutproject.Event.infrastructure.persistence.model.organizer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizerJPARepository extends JpaRepository<Organizer, Long> {
}
