package at.fhv.Authors.events.ui;

import at.fhv.Authors.domain.model.Event;
import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class EventTemplateProvider {
    @GetMapping("/events")
    public ModelAndView getEventTemplate() {
        System.out.println("getEventTemplate");
        // Example event list
        List<Event> events = Arrays.asList(
                new Event("Bubble Soccer", "", "Dornbirn", LocalDate.of(2025, 11, 10), new BigDecimal("44.99")),
                new Event ("Skydiving", "", "Hohenems", LocalDate.of(2025, 12, 5), new BigDecimal("299.99"))

        );

        return new ModelAndView("Events", "events", events);
    }

}
