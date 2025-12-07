package everoutproject.Event.domain.model.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    
    void addNewUser(User domainUser);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void save(User domainUser);
    void delete(Long id);
}
