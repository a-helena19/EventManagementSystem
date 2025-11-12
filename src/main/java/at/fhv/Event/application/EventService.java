package at.fhv.Event.application;

import at.fhv.Event.domain.model.Event;
import at.fhv.Event.domain.model.EventImage;
import at.fhv.Event.domain.model.Status;
import at.fhv.Event.persistence.EventImageRepository;
import at.fhv.Event.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventImageRepository eventImageRepository;

    public Event createEvent(String name, String description, String location, LocalDate date,
                             BigDecimal price, List<MultipartFile> images) throws Exception {
        Event newEvent = new Event();
        newEvent.setName(name);
        newEvent.setDescription(description);
        newEvent.setLocation(location);
        newEvent.setDate(date);
        newEvent.setPrice(price);
        newEvent.setStatus(Status.ACTIVE);

        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    EventImage eventImage = new EventImage(file.getBytes(), newEvent);
                    newEvent.addImage(eventImage);
                }
            }
        }

        return eventRepository.save(newEvent);

    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public byte[] getEventImage(Long id) {
        EventImage image = eventImageRepository.findById(id).orElseThrow();
        return image.getImageData();
    }

    public Event cancelEvent(Long id, String reason) {
        Event eventToCancel = eventRepository.findById(id).orElseThrow();
        eventToCancel.setStatus(Status.CANCELLED);
        eventToCancel.setCancellationReason(reason);
        return eventRepository.save(eventToCancel);
    }
}
