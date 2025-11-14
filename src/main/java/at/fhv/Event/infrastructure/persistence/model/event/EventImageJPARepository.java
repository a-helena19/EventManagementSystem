package at.fhv.Event.infrastructure.persistence.model.event;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventImageJPARepository extends JpaRepository<EventImage, Long> {
}
