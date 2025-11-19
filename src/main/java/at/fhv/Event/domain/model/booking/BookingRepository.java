package at.fhv.Event.domain.model.booking;

import java.util.List;

public interface BookingRepository {

    void addNewEvent(Booking domainBooking);
    List<Booking> findAll();
}
