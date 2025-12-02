package everoutproject.Event.rest.dtos.event.response;

import java.math.BigDecimal;

public record AdditionalPackageDTO(
        Long id,
        String title,
        String description,
        BigDecimal price
) {}
