package everoutproject.Event.application.services;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.domain.model.event.*;
import everoutproject.Event.rest.dtos.event.EventDTO;
import everoutproject.Event.application.dtos.EventMapperDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @PreAuthorize("@roleChecker.canCreateEvent(authentication)")
    public EventDTO createEvent(String name, String description, EventLocation location, LocalDate date,
                                BigDecimal price, List<MultipartFile> images) throws Exception {

        List<EventImage> imageList = images != null
                ? images.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    try { return new EventImage(null, file.getBytes()); }
                    catch (Exception e) { throw new RuntimeException(e); }
                }).collect(Collectors.toList()) : List.of();

        Event newEvent = new Event(name, description, location, date, price, EventStatus.ACTIVE);
        imageList.forEach(newEvent::addImage);

        eventRepository.addNewEvent(newEvent);

        // Nach dem Speichern abrufen, um die ID zu erhalten
        Event persistedEvent = eventRepository.findAll().stream()
                .filter(e -> e.getName().equals(name) && e.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found after save"));

        return EventMapperDTO.toDTO(persistedEvent);
    }

    public List<EventDTO> getAllEventsDTO() {
        return eventRepository.findAll().stream()
                .map(EventMapperDTO::toDTO)
                .collect(Collectors.toList());
    }


    public byte[] getEventImage(Long id) {
        return eventRepository.findImageById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"))
                .getImageData();
    }


    @PreAuthorize("@roleChecker.canCancelEvent(authentication)")
    public void cancelEvent(Long id, String reason) {
        Event eventToCancel = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        eventToCancel.cancel(reason);
        eventRepository.save(eventToCancel);

        // Alle Buchungen für dieses Event stornieren
        List<Booking> bookings = bookingRepository.findByEventId(id);
        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.EVENTCANCELLED);
            bookingRepository.save(booking);
        }
    }


    @PreAuthorize("@roleChecker.canEditEvent(authentication)")
    public void editEvent(Long id, String name, String description, EventLocation location, LocalDate date,
                          BigDecimal price, EventStatus status, List<MultipartFile> images,
                          List<Long> idsToDelete) throws Exception {

        Event eventToEdit = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<EventImage> imageList = new ArrayList<>();
        if (images != null) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    imageList.add(new EventImage(null, file.getBytes()));
                }
            }
        }

        if (idsToDelete != null && !idsToDelete.isEmpty()) {
            List<EventImage> toRemove = eventToEdit.getImages().stream()
                    .filter(img -> img.getId() != null && idsToDelete.contains(img.getId()))
                    .toList();
            toRemove.forEach(eventToEdit::removeImage);
        }

        List<EventImage> mergedImages = eventToEdit.getImages() != null
                ? new ArrayList<>(eventToEdit.getImages()) : new ArrayList<>();
        mergedImages.addAll(imageList);

        eventToEdit.edit(name, description, location, date, price, status, mergedImages);
        eventRepository.save(eventToEdit);
    }


    @Transactional
    public void printEvents() {
        eventRepository.findAll().forEach(event -> System.out.println(event.toString()));
    }
}
