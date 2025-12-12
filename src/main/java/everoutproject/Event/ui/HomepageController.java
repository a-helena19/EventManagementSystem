package everoutproject.Event.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HomepageController {

    // create a view and link it to Homepage.html
    @GetMapping("/homepage")
    public ModelAndView homepage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        ModelAndView mv = new ModelAndView("Homepage");
        mv.addObject("user", user);
        return mv;
    }


    // Root mapping also goes to homepage
    @GetMapping("/")
    public String redirectToHomepage() {
        return "redirect:/homepage";
    }

}