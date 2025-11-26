package everoutproject.Event.application.scheduling;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventRepository;
import everoutproject.Event.domain.model.event.EventStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class EventExpirationScheduler {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventExpirationScheduler(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    // runs every 10 sec
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void expireOldEvents() {

        LocalDate today = LocalDate.now();

        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            if (event.getDate().isBefore(today) && event.getStatus() == EventStatus.ACTIVE) {

                // set Event status on EXPIRED
                event.setStatus(EventStatus.EXPIRED);
                eventRepository.save(event);

                List<Booking> bookings = bookingRepository.findByEventId(event.getId());

                // set Booking status on EXPIRED
                for (Booking booking : bookings) {
                    booking.setStatus(BookingStatus.EXPIRED);
                    bookingRepository.save(booking);
                }

                System.out.println("Event expired: " + event.getId());
            }
        }
    }
}
