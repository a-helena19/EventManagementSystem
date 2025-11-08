package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.domain.model.EventImage;
import at.fhv.Authors.domain.model.Status;
import at.fhv.Authors.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Controller
public class HomepageTemplateProvider {

    @Autowired
    private EventRepository eventRepository;

    // create a view and link it to Homepage.html
    @GetMapping("/homepage")
    public ModelAndView getHomepageTemplate() {
        System.out.println("getHomepageTemplate called!");
        return new ModelAndView("Homepage");
    }

    // Root mapping also goes to homepage
    @GetMapping("/")
    public String redirectToHomepage() {
        return "redirect:/homepage";
    }

    // RequestParam: gets data by using name in html not id or class
    @PostMapping("/create-event")
    public String createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String location,
            @RequestParam LocalDate date,
            @RequestParam BigDecimal price,
            @RequestParam List<MultipartFile> images,
            RedirectAttributes redirectAttributes
    ) { try {
        System.out.println("Creating new Event: " + name);

        Event newEvent = new Event();
        newEvent.setName(name);
        newEvent.setDescription(description);
        newEvent.setLocation(location);
        newEvent.setDate(date);
        newEvent.setPrice(price);
        newEvent.setStatus(Status.ACTIVE); // we might change this later to be confirmed by back-office

        // Simulate Error Toast Notifaction
//           if (name.equalsIgnoreCase("error")) {
//                throw new RuntimeException("Simulierter Fehler für Test!");
//           }


        // add images
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    EventImage eventImage = new EventImage(file.getBytes(), newEvent);
                    newEvent.addImage(eventImage);
                }
            }
        }

        eventRepository.save(newEvent);

        // Success message
        redirectAttributes.addFlashAttribute("toastType", "success");
        redirectAttributes.addFlashAttribute("toastMessage", "Event '" + name + "' successfully created!");

    } catch (Exception e) {
        System.err.println("Error creating event: " + e.getMessage());
        e.printStackTrace();

        // Error message
        redirectAttributes.addFlashAttribute("toastType", "error");
        redirectAttributes.addFlashAttribute("toastMessage", "Failed to create event. Please try again.");
    }

        return "redirect:/homepage";
    }

}