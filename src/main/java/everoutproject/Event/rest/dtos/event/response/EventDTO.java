package everoutproject.Event.rest.dtos.event.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EventDTO(
        Long id,
        String name,
        String description,
        EventLocationDTO location,  // structured

        LocalDate startDate,
        LocalDate endDate,

        List<EventAppointmentDTO> appointments,

        BigDecimal price,
        Integer depositPercent,
        String status,
        String cancellationReason,

        Integer minParticipants,
        Integer maxParticipants,
        Integer bookedParticipants,

        List<RequirementDTO> requirements,
        List<EventEquipmentDTO> equipment,
        List<AdditionalPackageDTO> additionalPackages,

        String category,
        Long organizerId,
        OrganizerDTO organizer,
        Integer durationInDays,

        List<EventFeedbackDTO> feedback,
        List<Long> imageIds
) {}
