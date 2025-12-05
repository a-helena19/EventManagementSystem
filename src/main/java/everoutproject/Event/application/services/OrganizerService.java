package everoutproject.Event.application.services;

import everoutproject.Event.application.dtos.OrganizerMapperDTO;
import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.domain.model.organizer.OrganizerRepository;
import everoutproject.Event.rest.dtos.event.request.NewOrganizerRequestDTO;
import everoutproject.Event.rest.dtos.event.response.OrganizerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    public OrganizerService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    public OrganizerDTO createOrganizer(NewOrganizerRequestDTO dto) {
        Organizer domain = new Organizer(null, dto.name, dto.email, dto.phone);

        Organizer saved = organizerRepository.save(domain);
        return new OrganizerDTO(saved.getId(), saved.getName(), saved.getContactEmail(), saved.getPhone());

    }

    public List<OrganizerDTO> getAllOrganizers() {
        return organizerRepository.findAll()
                .stream()
                    .map(OrganizerMapperDTO::toDTO)
                .toList();
    }
}
