package everoutproject.Event.infrastructure.persistence.model.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingJPARepository extends JpaRepository<Booking, Long> {

    // JPA will automatically create a sql query: SELECT * FROM Booking WHERE event_id = :eventId
    // Important: methode name must have the exact same name as the table field in the entity
    List<Booking> findByEventId(Long eventId);
}
