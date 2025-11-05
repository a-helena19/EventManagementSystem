package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.domain.model.EventImage;
import at.fhv.Authors.domain.model.Status;
import at.fhv.Authors.persistence.EventImageRepository;
import at.fhv.Authors.persistence.EventRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class EventTemplateProvider {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventImageRepository eventImageRepository;

    @GetMapping("/events")
    public ModelAndView getEventTemplate() {
        // Test if it gets here
        System.out.println("getEventTemplate called!");

        // Example event list
        List<Event> events = eventRepository.findAll();

        return new ModelAndView("Events", "events", events);
    }

    @GetMapping("/events/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getEventImage(@PathVariable Long id) {
        EventImage image = eventImageRepository.findById(id).orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // oder MediaType.IMAGE_PNG je nach Format

        return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
    }

    @PostMapping("/create-sample")
    public String createSampleEvent() {
        System.out.println("createSampleEvent called!");
        eventRepository.save(new Event("Button Event", "Testing the create Event button", "Berlin", LocalDate.of(2026, 11, 20), new BigDecimal("199.99"), Status.ACTIVE));
        return "redirect:/events";
    }
    
    
    @PostMapping("/cancel_event/{id}")
    public String cancelEvent(
        @PathVariable Long id,
        @RequestParam ("cancellationReason") String reason

    ) {
        System.out.println("cancelEvent called!");
        Event eventToCancel = eventRepository.findById(id).orElseThrow();

        eventToCancel.setStatus(Status.CANCELLED);
        eventToCancel.setCancelreason(reason);

        // JPA repository will take save() here as Update because we get the Event with its id
        eventRepository.save(eventToCancel);
        return "redirect:/events";
    }

}
