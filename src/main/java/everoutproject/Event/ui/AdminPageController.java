package everoutproject.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public ModelAndView getAdminPage() {
        return new ModelAndView("Admin");
    }
}
