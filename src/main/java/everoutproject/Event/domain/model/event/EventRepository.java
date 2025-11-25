package everoutproject.Event.domain.model.event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    void addNewEvent(Event domainEvent);
    Optional<Event> findById(Long id);
    List<Event> findAll();
    void save(Event domainEvent);
    Optional<EventImage> findImageById(Long id);

}
