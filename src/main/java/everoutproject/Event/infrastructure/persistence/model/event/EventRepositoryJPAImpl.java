package everoutproject.Event.infrastructure.persistence.model.event;

import everoutproject.Event.domain.model.event.EventRepository;
import everoutproject.Event.domain.model.event.EventImage;
import everoutproject.Event.infrastructure.mapper.EventMapper;
import everoutproject.Event.infrastructure.mapper.OrganizerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventRepositoryJPAImpl implements EventRepository {

    private final EventJPARepository eventJpaRepository;
    private final EventImageJPARepository eventImageJpaRepository;

    public EventRepositoryJPAImpl(EventJPARepository eventJpaRepository,
                                  EventImageJPARepository eventImageJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
        this.eventImageJpaRepository = eventImageJpaRepository;
    }

    @Override
    public everoutproject.Event.domain.model.event.Event addNewEvent(everoutproject.Event.domain.model.event.Event domainEvent) {
        everoutproject.Event.infrastructure.persistence.model.event.Event entity = EventMapper.toEntity(domainEvent);
        everoutproject.Event.infrastructure.persistence.model.event.Event savedEntity = eventJpaRepository.save(entity);

       return EventMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<everoutproject.Event.domain.model.event.Event> findById(Long id) {
        return eventJpaRepository.findById(id)
                .map(EventMapper::toDomain);
    }

    @Override
    public List<everoutproject.Event.domain.model.event.Event> findAll() {
        return eventJpaRepository.findAll().stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public void save(everoutproject.Event.domain.model.event.Event domainEvent) {
        eventJpaRepository.findById(domainEvent.getId())
                .ifPresentOrElse(entity -> {

                    // ================
                    // UPDATE SCALAR FIELDS
                    // ================
                    entity.setName(domainEvent.getName());
                    entity.setDescription(domainEvent.getDescription());

                    entity.setStreet(domainEvent.getLocation().getStreet());
                    entity.setHouseNumber(domainEvent.getLocation().getHouseNumber());
                    entity.setCity(domainEvent.getLocation().getCity());
                    entity.setPostalCode(domainEvent.getLocation().getPostalCode());
                    entity.setState(domainEvent.getLocation().getState());
                    entity.setCountry(domainEvent.getLocation().getCountry());

                    entity.setStartDate(domainEvent.getStartDate());
                    entity.setEndDate(domainEvent.getEndDate());

                    entity.setPrice(domainEvent.getPrice());
                    entity.setStatus(EventMapper.toEntityStatus(domainEvent.getStatus()));
                    entity.setCancellationReason(domainEvent.getCancellationReason());

                    entity.setCategory(EventMapper.toEntityCategory(domainEvent.getCategory()));
                    entity.setOrganizer(
                            OrganizerMapper.toEntity(domainEvent.getOrganizer())
                    );


                    entity.getAppointments().clear();
                    domainEvent.getAppointments()
                            .forEach(a -> entity.addAppointment(
                                    new EventAppointment(
                                            a.getId(),
                                            a.getStartDate(),
                                            a.getEndDate(),
                                            a.isSeasonal(),
                                            entity
                                    )
                            ));
                    // ========================
                    // UPDATE REQUIREMENTS
                    // ========================
                    entity.getRequirements().clear();
                    domainEvent.getRequirements()
                            .forEach(r -> entity.addRequirement(
                                    new Requirement(
                                            r.getId(),
                                            r.getDescription(),
                                            entity
                                    )
                            ));

                    // ========================
                    // UPDATE EQUIPMENT
                    // ========================
                    entity.getEquipments().clear();
                    domainEvent.getEquipment()
                            .forEach(eq -> entity.addEquipment(
                                    new EventEquipment(
                                            eq.getId(),
                                            eq.getName(),
                                            eq.isRentable(),
                                            entity
                                    )
                            ));

                    // ========================
                    // UPDATE PACKAGES
                    // ========================
                    entity.getAdditionalPackages().clear();
                    domainEvent.getAdditionalPackages()
                            .forEach(p -> entity.addAdditionalPackage(
                                    new AdditionalPackage(
                                            p.getId(),
                                            p.getTitle(),
                                            p.getDescription(),
                                            p.getPrice(),
                                            entity
                                    )
                            ));

                    // ================
                    // UPDATE IMAGES (MERGE, DO NOT RECREATE!)
                    // ================

                    // 1. Remove images that are not in the aggregate anymore
                    entity.getImages().removeIf(persistedImg ->
                            domainEvent.getImages().stream()
                                    .noneMatch(domainImg ->
                                            domainImg.getId() != null &&
                                                    domainImg.getId().equals(persistedImg.getId()))
                    );

                    // 2. Update existing or add new images
                    for (var domainImg : domainEvent.getImages()) {

                        // Check if image already exists in DB
                        var existing = entity.getImages().stream()
                                .filter(x -> domainImg.getId() != null &&
                                        x.getId().equals(domainImg.getId()))
                                .findFirst();

                        if (existing.isPresent()) {
                            // Update binary data
                            existing.get().setImageData(domainImg.getImageData());
                        } else {
                            // Convert domain image → persistence image
                            var newImgEntity = EventMapper.toEntityImage(domainImg);
                            newImgEntity.setEvent(entity);
                            entity.getImages().add(newImgEntity);
                        }
                    }

                    // Save updated event
                    eventJpaRepository.save(entity);

                }, () -> {
                    // If event does not exist, insert new one
                    Event newEntity = EventMapper.toEntity(domainEvent);
                    eventJpaRepository.save(newEntity);
                });
    }

    @Override
    public Optional<everoutproject.Event.domain.model.event.EventImage> findImageById(Long id) {
        return eventImageJpaRepository.findById(id)
                .map(img -> new EventImage(img.getId(), img.getImageData()));
    }

}
