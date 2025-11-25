package everoutproject.Event.application.dtos;

import everoutproject.Event.rest.dtos.event.EventDTO;
import everoutproject.Event.rest.dtos.event.EventLocationDTO;
import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventLocation;

import java.util.stream.Collectors;

public class EventMapperDTO {

    public static EventDTO toDTO(Event event) {
        return new EventDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                toLocationDTO(event.getLocation()),  // structured location
                event.getDate(),
                event.getPrice(),
                event.getStatus().name(),
                event.getCancellationReason(),
                event.getImages().stream().map(img -> img.getId()).collect(Collectors.toList())
        );
    }

    private static EventLocationDTO toLocationDTO(EventLocation location) {
        if (location == null) return null;
        return new EventLocationDTO(
                location.getStreet(),
                location.getHouseNumber(),
                location.getCity(),
                location.getPostalCode(),
                location.getState(),
                location.getCountry()
        );
    }
}
