package everoutproject.Event.infrastructure.persistence.model.user;

import everoutproject.Event.domain.model.user.User;
import everoutproject.Event.domain.model.user.UserRepository;
import everoutproject.Event.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryJPAImpl implements UserRepository {

    private final UserJPARepository userJPARepository;

    public UserRepositoryJPAImpl(UserJPARepository userJPARepository) {
        this.userJPARepository = userJPARepository;
    }

    @Override
    public void addNewUser(User domainUser) {
        everoutproject.Event.infrastructure.persistence.model.user.User entity = UserMapper.toEntity(domainUser);
        userJPARepository.save(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJPARepository.findById(id)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJPARepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJPARepository.findAll().stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(User domainUser) {
        everoutproject.Event.infrastructure.persistence.model.user.User entity = UserMapper.toEntity(domainUser);
        userJPARepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        userJPARepository.deleteById(id);
    }
}
