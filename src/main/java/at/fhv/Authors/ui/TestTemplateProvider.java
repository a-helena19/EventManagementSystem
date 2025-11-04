package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.domain.model.Status;
import at.fhv.Authors.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class TestTemplateProvider {
    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/test")
    public ModelAndView getTestTemplate() {
        // Test if it gets here
        System.out.println("getTestTemplate called!");


        return new ModelAndView("Test");
    }
}
