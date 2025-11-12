package at.fhv.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EventPageController {


    //create a view and link it to Events.html
    @GetMapping("/events")
    public ModelAndView getEventTemplate() {
        return new ModelAndView("Events");
    }

}
