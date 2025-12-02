package everoutproject.Event.domain.model.booking;

import java.util.List;

public interface BookingRepository {

    void addNewEvent(Booking domainBooking);
    List<Booking> findAll();
    List<Booking> findByEventId(Long eventId);
    List<Booking> findByEmail(String email);
    List<Booking> findByUserId(Long userId);
    void save(Booking booking);
}
