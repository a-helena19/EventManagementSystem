package everoutproject.Event.rest.dtos.user;

import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String role,
        LocalDate createdAt,
        LocalDate updatedAt
) {}
