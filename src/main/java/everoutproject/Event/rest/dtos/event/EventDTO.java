package everoutproject.Event.rest.dtos.event;

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
        String status,
        String cancellationReason,

        Integer minParticipants,
        Integer maxParticipants,

        List<RequirementDTO> requirements,
        List<EventEquipmentDTO> equipment,
        List<AdditionalPackageDTO> additionalPackages,

        String category,
        OrganizerDTO organizer,
        Integer durationInDays,

        List<EventFeedbackDTO> feedback,
        List<Long> imageIds
) {}
