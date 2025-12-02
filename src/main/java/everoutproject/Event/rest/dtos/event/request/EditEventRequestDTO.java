package everoutproject.Event.rest.dtos.event.request;

import everoutproject.Event.rest.dtos.event.response.EventLocationDTO;

import java.time.LocalDate;
import java.util.List;

public class EditEventRequestDTO {

    public Long id;

    public String name;
    public String description;

    public LocalDate startDate;
    public LocalDate endDate;

    public Double price;
    public Integer depositPercent;

    public String category;

    public Long organizerId;
    public NewOrganizerRequestDTO newOrganizer;

    public Integer minParticipants;
    public Integer maxParticipants;

    public EventLocationDTO location;

    public List<AppointmentRequestDTO> appointments;
    public List<RequirementRequestDTO> requirements;
    public List<EquipmentRequestDTO> equipment;
    public List<AdditionalPackageRequestDTO> additionalPackages;

}