package everoutproject.Event.infrastructure.persistence.model.organizer;

import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.domain.model.organizer.OrganizerRepository;
import everoutproject.Event.infrastructure.mapper.OrganizerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<Organizer> findAll() {
        return organizerJPARepository.findAll()
                .stream()
                .map(OrganizerMapper::toDomain)
                .toList();
    }

    @Override
    public Organizer save(Organizer domainOrg) {
        everoutproject.Event.infrastructure.persistence.model.organizer.Organizer entity = OrganizerMapper.toEntity(domainOrg);
        everoutproject.Event.infrastructure.persistence.model.organizer.Organizer saved = organizerJPARepository.save(entity);
        return OrganizerMapper.toDomain(saved);
    }
}
