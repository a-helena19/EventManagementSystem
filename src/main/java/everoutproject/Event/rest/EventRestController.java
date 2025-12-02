package everoutproject.Event.rest;

import everoutproject.Event.application.services.EventService;
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
import java.net.URLConnection;
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
    ) {
        try {
            EventDTO created = eventService.createEvent(dto, images);
            System.out.println("Created event: " + created);
            System.out.println("ID = " + created.id());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Event created successfully",
                            "id", created.id(),
                            "name", created.name()
                    ));
        } catch (Exception e) {
            e.printStackTrace();
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
    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editEvent(@PathVariable Long id,
                                       @RequestPart("event") EditEventRequestDTO dto,
                                       @RequestPart(value =  "images", required = false) List<MultipartFile> images,
                                       @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) {
        try {
            EventDTO updated = eventService.editEvent(id, dto, images, deleteImageIds);
            return ResponseEntity.ok(Map.of(
                    "message", "Event was edited successfully",
                    "id", updated.id(),
                    "name", updated.name()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to edit event: " + e.getMessage()));
        }
    }
}
