package at.fhv.Event.infrastructure.persistence.model.event;

import at.fhv.Event.domain.model.event.EventRepository;
import at.fhv.Event.domain.model.event.Event;
import at.fhv.Event.domain.model.event.EventImage;
import at.fhv.Event.infrastructure.mapper.EventMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void addNewEvent(Event domainEvent) {
        at.fhv.Event.infrastructure.persistence.model.event.Event entity = EventMapper.toEntity(domainEvent);
        eventJpaRepository.save(entity);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventJpaRepository.findById(id)
                .map(EventMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll().stream()
                .map(EventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Event eventAggregate) {
        eventJpaRepository.findById(eventAggregate.getId())
                .ifPresentOrElse(entity -> {

                    // ================
                    // UPDATE SCALAR FIELDS
                    // ================
                    entity.setName(eventAggregate.getName());
                    entity.setDescription(eventAggregate.getDescription());

                    entity.setStreet(eventAggregate.getLocation().getStreet());
                    entity.setHouseNumber(eventAggregate.getLocation().getHouseNumber());
                    entity.setCity(eventAggregate.getLocation().getCity());
                    entity.setPostalCode(eventAggregate.getLocation().getPostalCode());
                    entity.setState(eventAggregate.getLocation().getState());
                    entity.setCountry(eventAggregate.getLocation().getCountry());

                    entity.setDate(eventAggregate.getDate());
                    entity.setPrice(eventAggregate.getPrice());
                    entity.setStatus(EventMapper.toEntityStatus(eventAggregate.getStatus()));
                    entity.setCancellationReason(eventAggregate.getCancellationReason());

                    // ================
                    // UPDATE IMAGES (MERGE, DO NOT RECREATE!)
                    // ================

                    // 1. Remove images that are not in the aggregate anymore
                    entity.getImages().removeIf(persistedImg ->
                            eventAggregate.getImages().stream()
                                    .noneMatch(domainImg ->
                                            domainImg.getId() != null &&
                                                    domainImg.getId().equals(persistedImg.getId()))
                    );

                    // 2. Update existing or add new images
                    for (var domainImg : eventAggregate.getImages()) {

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
                    eventJpaRepository.save(EventMapper.toEntity(eventAggregate));
                });
    }

    @Override
    public Optional<EventImage> findImageById(Long id) {
        return eventImageJpaRepository.findById(id)
                .map(img -> new EventImage(img.getId(), img.getImageData()));
    }

}
