package at.fhv.Authors.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HomepageTemplateProvider {
    @GetMapping("/homepage")
    public ModelAndView getHomepageTemplate() {
        // Test if it gets here
        System.out.println("getHomepageTemplate called!");


        return new ModelAndView("Homepage");
    }
}
