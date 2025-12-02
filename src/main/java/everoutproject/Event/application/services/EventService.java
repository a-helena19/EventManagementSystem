package everoutproject.Event.application.services;

import everoutproject.Event.domain.model.booking.Booking;
import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.domain.model.booking.BookingStatus;
import everoutproject.Event.domain.model.event.*;
import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.domain.model.organizer.OrganizerRepository;
import everoutproject.Event.rest.dtos.event.request.CreateEventRequestDTO;
import everoutproject.Event.rest.dtos.event.request.EditEventRequestDTO;
import everoutproject.Event.rest.dtos.event.response.EventDTO;
import everoutproject.Event.application.dtos.EventMapperDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final OrganizerRepository organizerRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository, OrganizerRepository organizerRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.organizerRepository = organizerRepository;
    }

    /**
     * Create a new Event with optional images.
     */
    @Transactional
    public EventDTO createEvent(CreateEventRequestDTO dto, List<MultipartFile> images) {

        Organizer organizer;

        if (dto.newOrganizer != null) {
            // neu organizer from dto
            organizer = organizerRepository.save(
                    new Organizer(null, dto.newOrganizer.name, dto.newOrganizer.email, dto.newOrganizer.phone)
            );
        } else {
            // if exists
            organizer = organizerRepository.findById(dto.organizerId)
                    .orElseThrow(() -> new RuntimeException("Organizer not found"));
        }

        EventCategory category = EventCategory.valueOf(dto.category);

        EventLocation location = new EventLocation(
                dto.location.street(),
                dto.location.houseNumber(),
                dto.location.city(),
                dto.location.postalCode(),
                dto.location.state(),
                dto.location.country()
        );

        Event newEvent = new Event(
                dto.name,
                dto.description,
                location,
                dto.startDate,
                dto.endDate,
                BigDecimal.valueOf(dto.price),
                dto.depositPercent,
                EventStatus.ACTIVE,
                category,
                organizer
        );

        newEvent.setMinParticipants(dto.minParticipants);
        newEvent.setMaxParticipants(dto.maxParticipants);

        // Add children
        if (dto.appointments != null) {
            dto.appointments.forEach(a ->
                    newEvent.addAppointment(
                            new EventAppointment(null, a.startDate, a.endDate, a.seasonal)
                    )
            );
        }

        if (dto.requirements != null) {
            dto.requirements.forEach(r ->
                    newEvent.addRequirement(
                            new Requirement(null, r.description)
                    )
            );
        }

        if (dto.equipment != null) {
            dto.equipment.forEach(e ->
                    newEvent.addEquipment(
                            new EventEquipment(null, e.name, e.rentable)
                    )
            );
        }
        if (dto.additionalPackages != null) {
            dto.additionalPackages.forEach(p ->
                    newEvent.addPackage(
                            new AdditionalPackage(null, p.title, p.description, p.price)
                    )
            );
        }

        // Handle images
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        newEvent.addImage(new EventImage(null, file.getBytes()));
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to process image", ex);
                    }
                }
            }
        }

        // Persist event
        Event saved = eventRepository.addNewEvent(newEvent);

        // Return DTO
        return EventMapperDTO.toDTO(saved);
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

        List<Booking> bookings = bookingRepository.findByEventId(id);
        if (!bookings.isEmpty()) {
            for (Booking booking : bookings) {
                booking.setStatus(BookingStatus.EVENTCANCELLED);
                bookingRepository.save(booking);
                System.out.println("Booking ID: " + booking.getId() + ", Status: " + booking.getStatus());
            }
        }
    }

    /**
     * Edit and Save an Event
     */
    @Transactional
    public EventDTO editEvent(Long id, EditEventRequestDTO dto, List<MultipartFile> images, List<Long> idsToDelete) throws Exception {

        Event eventToEdit = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer organizer;

        if (dto.newOrganizer != null) {
            organizer = new Organizer(
                    null,
                    dto.newOrganizer.name,
                    dto.newOrganizer.email,
                    dto.newOrganizer.phone
            );
        } else {
            organizer = organizerRepository.findById(dto.organizerId)
                    .orElseThrow(() -> new RuntimeException("Organizer not found"));
        }


        EventCategory category = EventCategory.valueOf(dto.category);

        EventLocation location = new EventLocation(
                dto.location.street(),
                dto.location.houseNumber(),
                dto.location.city(),
                dto.location.postalCode(),
                dto.location.state(),
                dto.location.country()
        );

        // --- Appointments ---
        List<EventAppointment> appointments = dto.appointments.stream()
                .map(a -> new EventAppointment(
                        a.id,        // keep existing ID if present!
                        a.startDate,
                        a.endDate,
                        a.seasonal
                ))
                .toList();

        // --- Requirements ---
        List<Requirement> requirements = dto.requirements.stream()
                .map(r -> new Requirement(r.id, r.description))
                .toList();

        // --- Equipment ---
        List<EventEquipment> equipment = dto.equipment.stream()
                .map(e -> new EventEquipment(e.id, e.name, e.rentable))
                .toList();

        // --- Packages ---
        List<AdditionalPackage> packages = dto.additionalPackages.stream()
                .map(p -> new AdditionalPackage(p.id, p.title, p.description, p.price))
                .toList();


        // Remove images that were marked for deletion
        if (idsToDelete != null && !idsToDelete.isEmpty()) {
            List<EventImage> toRemove = eventToEdit.getImages().stream()
                    .filter(img -> img.getId() != null && idsToDelete.contains(img.getId()))
                    .toList();

            toRemove.forEach(eventToEdit::removeImage);
        }

        if (images != null) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    try {
                        eventToEdit.addImage(new EventImage(null, file.getBytes()));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process image", e);
                    }

                }
            }
        }

        // --- Apply changes ---
        eventToEdit.edit(
                dto.name,
                dto.description,
                location,
                dto.startDate,
                dto.endDate,
                appointments,
                BigDecimal.valueOf(dto.price),
                dto.depositPercent,
                eventToEdit.getStatus(),   // status stays unchanged here
                dto.minParticipants,
                dto.maxParticipants,
                requirements,
                equipment,
                category,
                packages,
                organizer,
                eventToEdit.getDurationInDays(),
                eventToEdit.getImages()    // merged list
        );

        eventRepository.save(eventToEdit);

        return EventMapperDTO.toDTO(eventToEdit);
    }

    /**
     * For debugging/logging purposes: prints all events to console.
     */
    @Transactional
    public void printEvents() {
        eventRepository.findAll().forEach(event -> System.out.println(event.toString()));
    }
}
