package at.fhv.Authors.ui;

import at.fhv.Authors.domain.model.Event;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class TestTemplateProvider {
    @GetMapping("/test")
    public ModelAndView getTestTemplate() {
        // Test if it gets here
        System.out.println("getTestTemplate called!");


        return new ModelAndView("Test");
    }
}
