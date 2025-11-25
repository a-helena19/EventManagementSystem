package everoutproject.Event;

import everoutproject.Event.application.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EventApplicationRunner implements ApplicationRunner {


    @Autowired
    private EventService eventService;

    // this method will run everytime you start the application
    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("EventRunner is running!");

        eventService.printEvents();

    }
}
