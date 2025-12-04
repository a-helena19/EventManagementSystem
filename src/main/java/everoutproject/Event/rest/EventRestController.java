package everoutproject.Event.rest;

import everoutproject.Event.application.services.EventService;
import everoutproject.Event.domain.model.event.EventStatus;
import everoutproject.Event.rest.dtos.event.EventDTO;
import everoutproject.Event.rest.dtos.event.CancelRequestDTO;
import everoutproject.Event.domain.model.event.EventLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import everoutproject.Event.rest.dtos.event.request.CreateEventRequestDTO;
import everoutproject.Event.rest.dtos.event.request.EditEventRequestDTO;
import everoutproject.Event.rest.dtos.event.response.EventDTO;
import everoutproject.Event.rest.dtos.event.request.CancelRequestDTO;
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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvent(
            @RequestPart("event") CreateEventRequestDTO dto,
            @RequestPart(value = "images") List<MultipartFile> images
    ) throws Exception {

        EventLocation location = new EventLocation(street, houseNumber, city, postalCode, state, country);
        EventDTO eventDTO = eventService.createEvent(name, description, location, date, price, images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Event created successfully",
                        "id", eventDTO.id(),
                        "name", eventDTO.name()
                ));
    }

    // Return all events as DTOs
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEventsDTO());
    }

    // Return image bytes for a specific image ID
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getEventImage(@PathVariable Long id) throws Exception {
        byte[] imageData = eventService.getEventImage(id);

        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageData));
        if (contentType == null) {
            contentType = MediaType.IMAGE_JPEG_VALUE;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }


    // Cancel an event
    @PutMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancelEvent(
            @PathVariable Long id,
            @RequestBody CancelRequestDTO request
    ) {
        eventService.cancelEvent(id, request.getReason());
        return ResponseEntity.ok(Map.of("message", "Event cancelled successfully"));
    }

    // Edit an event
    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editEvent(@PathVariable Long id,
                                       @RequestPart("event") EditEventRequestDTO dto,
                                       @RequestPart(value =  "images", required = false) List<MultipartFile> images,
                                       @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds)
 throws Exception {

            EventLocation location = new EventLocation(street, houseNumber, city, postalCode, state, country);
            EventStatus eventStatus = EventStatus.valueOf(status);

            List<Long> idsToDelete = null;
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                idsToDelete = new ObjectMapper().readValue(deleteImageIds, new TypeReference<List<Long>>() {});
            }

            eventService.editEvent(id, name, description, location, date, price, eventStatus, images, idsToDelete);

            return ResponseEntity.ok(Map.of(
                    "message", "Event was edited successfully",
                    "id", id,
                    "name", name
            ));
    }
}
