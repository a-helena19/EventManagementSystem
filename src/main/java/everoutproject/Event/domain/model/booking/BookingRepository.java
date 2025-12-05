package everoutproject.Event.domain.model.booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    void addNewBooking(Booking domainBooking);
    List<Booking> findAll();
    List<Booking> findByEventId(Long eventId);
    List<Booking> findByEmail(String email);
    List<Booking> findByUserId(Long userId);
    Optional<Booking> findById(Long id);
    void save(Booking booking);
}
