package at.fhv.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BookingPageController {


    @GetMapping("/bookings")
    public ModelAndView getBookingsTemplate() {
        return new ModelAndView("Bookings");
    }
}
