package at.fhv.Event.application.services;

import at.fhv.Event.domain.model.event.*;
import at.fhv.Event.rest.dtos.event.EventDTO;
import at.fhv.Event.application.dtos.EventMapperDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Create a new Event with optional images.
     */
    public EventDTO createEvent(String name, String description, EventLocation location, LocalDate date,
                                BigDecimal price, List<MultipartFile> images) throws Exception {

        // Convert uploaded files to domain EventImage
        List<EventImage> imageList = images != null
                ? images.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    try {
                        return new EventImage(null, file.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList())
                : List.of();

        // Create domain Event
        Event newEvent = new Event(name, description, location, date, price, EventStatus.ACTIVE);
        imageList.forEach(newEvent::addImage);

        // Persist event
        eventRepository.addNewEvent(newEvent);

        // Retrieve the persisted event from the repository to ensure IDs are set
        Event persistedEvent = eventRepository.findAll().stream()
                .filter(e -> e.getName().equals(name) && e.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found after save"));

        // Return DTO
        return EventMapperDTO.toDTO(persistedEvent);
    }

    /**
     * Return all events as DTOs.
     */
    public List<EventDTO> getAllEventsDTO() {
        return eventRepository.findAll().stream()
                .map(EventMapperDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Return image data for a specific image ID.
     */
    public byte[] getEventImage(Long id) {
        return eventRepository.findImageById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"))
                .getImageData();
    }

    /**
     * Cancel an event and save it.
     */
    public void cancelEvent(Long id, String reason) {
        Event eventToCancel = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        eventToCancel.cancel(reason);
        eventRepository.save(eventToCancel);
    }

    /**
     * Edit and Save an Event
     */
    public void editEvent(Long id, String name, String description, EventLocation location, LocalDate date,
                          BigDecimal price, EventStatus status, List<MultipartFile> images) throws Exception {
        Event eventToEdit = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Convert uploaded files to domain EventImage
        List<EventImage> imageList = images != null
                ? images.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    try {
                        return new EventImage(null, file.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList())
                : List.of();

        eventToEdit.edit(name, description, location, date, price, status, imageList);
        eventRepository.save(eventToEdit);
    }

    /**
     * For debugging/logging purposes: prints all events to console.
     */
    @Transactional
    public void printEvents() {
        eventRepository.findAll().forEach(event -> System.out.println(event.toString()));
    }
}
