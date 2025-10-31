package at.fhv.Authors;

import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.persistence.EventRepository;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Testing if the method is running
        System.out.println("EventRunner is running!");

        // Inserting a new line to the event table
        eventRepository.save(new Event("Default Event", "Testing the onload method", "Vienna", LocalDate.of(2025, 11, 8), new BigDecimal("49.99")));


        // equals SELECT * FROM Event and saving the output in a list of event objects
        List<Event> events = eventRepository.findAll();

        // Printing each event in console
        for (Event event : events) {
            System.out.println("printing events: \n");
            event.toString();
        }

    }
}
