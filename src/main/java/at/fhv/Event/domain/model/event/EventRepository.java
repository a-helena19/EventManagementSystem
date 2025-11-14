package at.fhv.Event.domain.model.event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    void addNewEvent(Event eventAggregate);
    Optional<Event> findById(Long id);
    List<Event> findAll();
    void save(Event eventAggregate);
    Optional<EventImage> findImageById(Long id);

}
