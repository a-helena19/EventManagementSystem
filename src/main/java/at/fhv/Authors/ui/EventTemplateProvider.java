package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.persistence.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/events")
    public ModelAndView getEventTemplate() {
        // Test if it gets here
        System.out.println("getEventTemplate called!");

        // Example event list
        List<Event> events = Arrays.asList(
                new Event("Bubble Soccer", "", "Dornbirn", LocalDate.of(2025, 11, 10), new BigDecimal("44.99")),
                new Event ("Skydiving", "", "Hohenems", LocalDate.of(2025, 12, 5), new BigDecimal("299.99"))

        );

        return new ModelAndView("Events", "events", events);
    }

    @PostMapping("/create-sample")
    public String createSampleEvent() {
        System.out.println("createSampleEvent called!");
        eventRepository.save(new Event("Button Event", "Testing the create Event button", "Berlin", LocalDate.of(2026, 11, 20), new BigDecimal("199.99")));
        return "redirect:/events";
    }

}
