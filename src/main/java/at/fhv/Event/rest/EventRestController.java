package at.fhv.Event.rest;

import at.fhv.Event.application.services.EventService;
import at.fhv.Event.domain.model.event.EventStatus;
import at.fhv.Event.rest.dtos.event.EventDTO;
import at.fhv.Event.rest.dtos.event.CancelRequestDTO;
import at.fhv.Event.domain.model.event.EventLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    // Create a new event
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String street,
            @RequestParam String houseNumber,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam LocalDate date,
            @RequestParam BigDecimal price,
            @RequestParam List<MultipartFile> images
    ) {
        try {
            EventLocation location = new EventLocation(street, houseNumber, city, postalCode, state, country);
            EventDTO eventDTO = eventService.createEvent(name, description, location, date, price, images);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Event created successfully",
                            "id", eventDTO.id(),
                            "name", eventDTO.name()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create event: " + e.getMessage()));
        }
    }

    // Return all events as DTOs
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEventsDTO());
    }

    // Return image bytes for a specific image ID
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getEventImage(@PathVariable Long id) {
        try {
            byte[] imageData = eventService.getEventImage(id);
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageData));
            if (contentType == null) contentType = MediaType.IMAGE_JPEG_VALUE;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cancel an event
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id, @RequestBody CancelRequestDTO request) {
        try {
            eventService.cancelEvent(id, request.getReason());
            return ResponseEntity.ok(Map.of("message", "Event cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to cancel event: " + e.getMessage()));
        }
    }

    // Edit an event
    @PutMapping("/edit/{id}/status/{status}")
    public ResponseEntity<?> editEvent(@PathVariable Long id,
                                       @PathVariable String status,
                                       @RequestParam String name,
                                       @RequestParam(required = false) String description,
                                       @RequestParam String street,
                                       @RequestParam String houseNumber,
                                       @RequestParam String city,
                                       @RequestParam String postalCode,
                                       @RequestParam String state,
                                       @RequestParam String country,
                                       @RequestParam LocalDate date,
                                       @RequestParam BigDecimal price,
                                       @RequestPart(required = false) List<MultipartFile> images,
                                       @RequestParam(required = false) String deleteImageIds) {
        try {
            EventLocation location = new EventLocation(street, houseNumber, city, postalCode, state, country);
            EventStatus eventStatus = EventStatus.valueOf(status);


            // Optional: parse IDs to delete
            List<Long> idsToDelete = null;
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                idsToDelete = new ObjectMapper().readValue(deleteImageIds, new TypeReference<List<Long>>() {});
            }

            eventService.editEvent(id, name, description, location, date, price, eventStatus, images, idsToDelete);
            return ResponseEntity.ok(Map.of(
                    "message", "Event was edited successfully",
                    "id", id,
                    "name", name));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to edit event: " + e.getMessage()));
        }
    }
}
