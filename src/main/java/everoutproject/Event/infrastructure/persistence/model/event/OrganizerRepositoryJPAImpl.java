package everoutproject.Event.infrastructure.persistence.model.event;

import everoutproject.Event.domain.model.event.Organizer;
import everoutproject.Event.domain.model.event.OrganizerRepository;
import everoutproject.Event.infrastructure.mapper.OrganizerMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrganizerRepositoryJPAImpl implements OrganizerRepository {

    private final OrganizerJPARepository organizerJPARepository;

    public OrganizerRepositoryJPAImpl(OrganizerJPARepository organizerJPARepository) {
        this.organizerJPARepository = organizerJPARepository;
    }

    @Override
    public Optional<Organizer> findById(Long id) {

        return organizerJPARepository.findById(id).map(OrganizerMapper::toDomain);
    }
}
