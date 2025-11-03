package at.fhv.Authors.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UsertestTemplateProvider {

    @GetMapping("/usertest")
    public ModelAndView getUsertestTemplate() {
        // Test if it gets here
        System.out.println("getUsertestTemplate called!");


        return new ModelAndView("Usertest");
    }







}
