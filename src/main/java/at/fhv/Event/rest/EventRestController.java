package at.fhv.Event.rest;

import at.fhv.Event.application.EventService;
import at.fhv.Event.domain.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String location,
            @RequestParam LocalDate date,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        try {
            Event newEvent = eventService.createEvent(name, description, location, date, price, images);
            return ResponseEntity.ok(newEvent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create event: " + e.getMessage());
        }
    }

    // return all events as a JSON
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }



    //get image (e.g. <img src="/api/events/image/5">)
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getEventImage(@PathVariable Long id) {
        try {
            byte[] imageData = eventService.getEventImage(id);

            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageData));
            if (contentType == null) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    //cancel event
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id, @RequestBody CancelRequest request) {
        try {
            Event updated = eventService.cancelEvent(id, request.getReason());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to cancel event: " + e.getMessage());
        }
    }

    // small DTO-class for the JSON Body
    public static class CancelRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
