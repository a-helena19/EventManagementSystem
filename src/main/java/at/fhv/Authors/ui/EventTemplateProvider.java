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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

@Controller
public class EventTemplateProvider {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventImageRepository eventImageRepository;

    //create a view and link it to Events.html
    @GetMapping("/events")
    public ModelAndView getEventTemplate() {
        // Test if it gets here
        System.out.println("getEventTemplate called!");

        // SELECT * FROM EVENTS
        List<Event> events = eventRepository.findAll();

        return new ModelAndView("Events", "events", events);
    }

    // RequestParam: gets data by using name in html not id or class
    @GetMapping("/events/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getEventImage(@PathVariable Long id) throws IOException {
        EventImage image = eventImageRepository.findById(id).orElseThrow();

        // Automatically detects the correct MIME type from the image bytes
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image.getImageData()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
    }

    // we don't need this method anymore, but it might help later
    /*
    @PostMapping("/create_sample")
    public String createSampleEvent() {
        System.out.println("createSampleEvent called!");
        eventRepository.save(new Event("Button Event", "Testing the create Event button", "Berlin", LocalDate.of(2026, 11, 20), new BigDecimal("199.99"), Status.ACTIVE));
        return "redirect:/events";
    }

     */


    @PostMapping("/cancel_event/{id}")
    public String cancelEvent(
            @PathVariable Long id,
            @RequestParam ("cancellationReason") String reason,
            RedirectAttributes redirectAttributes

    ) { try  {
        System.out.println("cancelEvent called!");
        Event eventToCancel = eventRepository.findById(id).orElseThrow();

        String eventName = eventToCancel.getName();

        eventToCancel.setStatus(Status.CANCELLED);
        eventToCancel.setCancelreason(reason);


        // JPA repository will take save() here as Update not Insert into because we get the Event by its id
        eventRepository.save(eventToCancel);

        // Success message
        redirectAttributes.addFlashAttribute("toastType", "success");
        redirectAttributes.addFlashAttribute("toastMessage", "Event '" + eventName + "' successfully cancelled!");

    } catch (Exception e) {
        System.err.println("Error cancelling event: " + e.getMessage());
        e.printStackTrace();

        // Error message
        redirectAttributes.addFlashAttribute("toastType", "error");
        redirectAttributes.addFlashAttribute("toastMessage", "Failed to cancel event. Please try again.");
    }


        return "redirect:/events";
    }

}
