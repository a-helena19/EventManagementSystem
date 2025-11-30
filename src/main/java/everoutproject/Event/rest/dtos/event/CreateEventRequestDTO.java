package everoutproject.Event.rest.dtos.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateEventRequestDTO(
        String name,
        String description,
        EventLocationDTO location,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        String category,      // eventCategory enum name
        Long organizerId,
        Integer minParticipants,
        Integer maxParticipants,
        List<EventAppointmentDTO> appointments,
        List<RequirementDTO> requirements,
        List<EventEquipmentDTO> equipment,
        List<AdditionalPackageDTO> additionalPackages
) {
}
