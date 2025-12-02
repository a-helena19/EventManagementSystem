package everoutproject.Event.rest.dtos.event.request;

import everoutproject.Event.rest.dtos.event.response.*;

import java.time.LocalDate;
import java.util.List;

public class CreateEventRequestDTO {
    public String name;
    public String description;

    public LocalDate startDate;
    public LocalDate endDate;

    public Double price;
    public Integer depositPercent;   // NEW!

    public String category;

    public Long organizerId;         // existing organizer
    public NewOrganizerRequestDTO newOrganizer; // NEW new-organizer block

    public Integer minParticipants;
    public Integer maxParticipants;

    public EventLocationDTO location;

    public List<RequirementRequestDTO> requirements;
    public List<EquipmentRequestDTO> equipment;
    public List<AdditionalPackageRequestDTO> additionalPackages;
    public List<AppointmentRequestDTO> appointments;
}
