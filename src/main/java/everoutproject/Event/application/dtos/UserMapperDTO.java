package everoutproject.Event.application.dtos;

import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.rest.dtos.user.UserDTO;


public class UserMapperDTO {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}