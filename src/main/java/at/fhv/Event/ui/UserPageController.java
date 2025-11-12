package at.fhv.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserPageController {

    @GetMapping("/user")
    public ModelAndView getUsertestTemplate() {
        return new ModelAndView("User");
    }







}
