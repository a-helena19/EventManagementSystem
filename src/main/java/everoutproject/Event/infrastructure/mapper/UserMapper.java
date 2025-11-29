package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.domain.model.user.UserRole;

public class UserMapper {

    // Converts infrastructure entity to domain object
    public static User toDomain(everoutproject.Event.infrastructure.persistence.model.user.User entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                UserRole.valueOf(entity.getRole().name()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Converts domain to entity
    public static everoutproject.Event.infrastructure.persistence.model.user.User toEntity(User domain) {
        if (domain == null) return null;

        everoutproject.Event.infrastructure.persistence.model.user.User entity = 
            new everoutproject.Event.infrastructure.persistence.model.user.User(
                domain.getEmail(),
                domain.getPassword(),
                domain.getFirstName(),
                domain.getLastName(),
                everoutproject.Event.infrastructure.persistence.model.user.UserRole.valueOf(domain.getRole().name()),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
        
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        return entity;
    }
}
