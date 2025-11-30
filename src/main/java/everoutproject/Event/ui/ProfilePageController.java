package everoutproject.Event.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class ProfilePageController {
    @GetMapping("/profile")
    public String profilePage() {
        return "Profile";

    }
}