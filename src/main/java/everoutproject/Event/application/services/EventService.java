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
    private final OrganizerRepository organizerRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository, OrganizerRepository organizerRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.organizerRepository = organizerRepository;
    }

    /**
     * Create a new Event with optional images.
     */
    @PreAuthorize("@roleChecker.canCreateEvent(authentication)")
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
                dto.cancelDeadline,
                BigDecimal.valueOf(dto.price),
                dto.depositPercent,
                EventStatus.ACTIVE,
                category,
                organizer
        );

        newEvent.setMinParticipants(dto.minParticipants);
        newEvent.setMaxParticipants(dto.maxParticipants);
        newEvent.calculateDuration();

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
        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.EVENTCANCELLED);
            bookingRepository.save(booking);
        }
    }

    /**
     * Edit and Save an Event
     */
    @PreAuthorize("@roleChecker.canEditEvent(authentication)")
    @Transactional
    public EventDTO editEvent(Long id, EditEventRequestDTO dto, List<MultipartFile> images, List<Long> idsToDelete) throws Exception {

        Event eventToEdit = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Organizer organizer;

        if (dto.newOrganizer != null) {
            Organizer newOrg = new Organizer(
                    null,
                    dto.newOrganizer.name,
                    dto.newOrganizer.email,
                    dto.newOrganizer.phone
            );
            organizer = organizerRepository.save(newOrg);
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
                .map(a -> {
                    if (a.id == null) {
                        // NEW appointment
                        return new EventAppointment(null, a.startDate, a.endDate, a.seasonal);
                    } else {
                        // EXISTING appointment
                        EventAppointment existing = eventToEdit.getAppointments().stream()
                                .filter(ap -> ap.getId().equals(a.id))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Appointment not found: " + a.id));

                        existing.setStartDate(a.startDate);
                        existing.setEndDate(a.endDate);
                        existing.setSeasonal(a.seasonal);
                        return existing;
                    }

                })
                .toList();

        // --- Requirements ---
        List<Requirement> requirements = dto.requirements.stream()
                .map(r -> {
                    if (r.id == null) {
                        return new Requirement(null, r.description);
                    } else {
                        Requirement existing = eventToEdit.getRequirements().stream()
                                .filter(req -> req.getId().equals(r.id))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Requirement not found: " + r.id));

                        existing.setDescription(r.description);
                        return existing;
                    }
                })
                .toList();

        // --- Equipment ---
        List<EventEquipment> equipment = dto.equipment.stream()
                .map(e -> {
                    if (e.id == null) {
                        return new EventEquipment(null, e.name, e.rentable);
                    } else {
                        EventEquipment existing = eventToEdit.getEquipment().stream()
                                .filter(eq -> eq.getId().equals(e.id))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Equipment not found: " + e.id));

                        existing.setName(e.name);
                        existing.setRentable(e.rentable);
                        return existing;
                    }
                })
                .toList();

        // --- Packages ---
        List<AdditionalPackage> packages = dto.additionalPackages.stream()
                .map(p -> {
                    if (p.id == null) {
                        return new AdditionalPackage(null, p.title, p.description, p.price);
                    } else {
                        AdditionalPackage existing = eventToEdit.getAdditionalPackages().stream()
                                .filter(pack -> pack.getId().equals(p.id))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Package not found: " + p.id));

                        existing.setTitle(p.title);
                        existing.setDescription(p.description);
                        existing.setPrice(p.price);
                        return existing;
                    }
                })
                .toList();



        List<EventImage> finalImages = new ArrayList<>(eventToEdit.getImages());

        if (idsToDelete != null && !idsToDelete.isEmpty()) {

            List<EventImage> toDelete = finalImages.stream()
                    .filter(img -> img.getId() != null && idsToDelete.contains(img.getId()))
                    .toList();

            finalImages.removeAll(toDelete);
        }

        if (images != null) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    finalImages.add(new EventImage(null, file.getBytes()));
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
                dto.cancelDeadline,
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
                null,
                finalImages    // final merged image list
        );

        eventToEdit.calculateDuration();

        eventRepository.save(eventToEdit);

        return EventMapperDTO.toDTO(eventToEdit);
    }


    @Transactional
    public void printEvents() {
        eventRepository.findAll().forEach(event -> System.out.println(event.toString()));
    }
}
