package everoutproject.Event.infrastructure.persistence.model.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJPARepository extends JpaRepository<User, Long> {
    
    // JPA will automatically create a sql query: SELECT * FROM users WHERE email = :email
    Optional<User> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
}