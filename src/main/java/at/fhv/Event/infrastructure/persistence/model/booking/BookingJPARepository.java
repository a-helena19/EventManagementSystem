package at.fhv.Event.infrastructure.persistence.model.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingJPARepository extends JpaRepository<Booking, Long> {
}
