package at.fhv.Event.rest.dtos.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EventDTO(
        Long id,
        String name,
        String description,
        EventLocationDTO location,  // structured
        LocalDate date,
        BigDecimal price,
        String status,
        String cancellationReason,
        List<Long> imageIds
) {}
