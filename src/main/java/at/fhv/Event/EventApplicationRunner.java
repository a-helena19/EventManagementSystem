package at.fhv.Event;

import at.fhv.Event.domain.model.Event;
import at.fhv.Event.domain.model.Status;
import at.fhv.Event.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class EventApplicationRunner implements ApplicationRunner {


    @Autowired
    private EventRepository eventRepository;

    // this method will run everytime you start the application
    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("EventRunner is running!");

        // equals SELECT * FROM Event and saving the output in a list of event objects
        List<Event> events = eventRepository.findAll();

        // Printing each event in console
        for (Event event : events) {
            System.out.println("printing events: \n");
            System.out.println(event.toString());
        }

    }
}
