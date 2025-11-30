package everoutproject.Event.rest.dtos.event;

import java.time.LocalDate;

public record EventAppointmentDTO(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        boolean seasonal
) {}
