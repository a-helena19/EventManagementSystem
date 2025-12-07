package everoutproject.Event.rest.dtos.user;

import java.time.LocalDate;

public record UserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        LocalDate createdAt,
        LocalDate updatedAt
) {}