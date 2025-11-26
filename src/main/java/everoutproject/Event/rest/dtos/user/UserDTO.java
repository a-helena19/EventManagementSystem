package everoutproject.Event.rest.dtos.user;

import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String email,
        String password,
        String firstName,
        String lastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
