package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;


@Controller
public class HomepageTemplateProvider {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/homepage")
    public ModelAndView getHomepageTemplate() {
        // Test if it gets here
        System.out.println("getHomepageTemplate called!");


        return new ModelAndView("Homepage");
    }


    @PostMapping("/create-event")
    public String createEvent(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String location,
            @RequestParam LocalDate date,
            @RequestParam BigDecimal price
    ) {
        System.out.println("Creating new Event: " + name);
        Event newEvent = new Event(name, description, location, date, price);
        eventRepository.save(newEvent);

        return "redirect:/homepage";
    }

}
