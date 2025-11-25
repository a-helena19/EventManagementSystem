package everoutproject.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HomepageController {

    // create a view and link it to Homepage.html
    @GetMapping("/homepage")
    public ModelAndView getHomepageTemplate() {
        return new ModelAndView("Homepage");
    }

    // Root mapping also goes to homepage
    @GetMapping("/")
    public String redirectToHomepage() {
        return "redirect:/homepage";
    }

}