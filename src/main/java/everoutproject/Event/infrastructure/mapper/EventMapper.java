package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.event.Event;
import everoutproject.Event.domain.model.event.EventLocation;
import everoutproject.Event.domain.model.event.EventStatus;
import everoutproject.Event.infrastructure.persistence.model.event.EventImage;

import java.util.List;

public class EventMapper {
    public static Event toDomain(everoutproject.Event.infrastructure.persistence.model.event.Event entity) {
        List<everoutproject.Event.domain.model.event.EventImage> images = entity.getImages().stream()
                .map(img -> new everoutproject.Event.domain.model.event.EventImage(img.getId(), img.getImageData()))
                .toList();
        EventLocation location = new EventLocation(entity.getStreet(), entity.getHouseNumber(), entity.getCity(),
                entity.getPostalCode(), entity.getState(), entity.getCountry());

        return new Event(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                location,
                entity.getDate(),
                entity.getPrice(),
                EventStatus.valueOf(entity.getStatus().name()),
                entity.getCancellationReason(),
                images
        );
    }

    public static everoutproject.Event.infrastructure.persistence.model.event.Event toEntity(Event domain) {

        everoutproject.Event.infrastructure.persistence.model.event.Event entity = new everoutproject.Event.infrastructure.persistence.model.event.Event(
                domain.getName(),
                domain.getDescription(),
                domain.getLocation().getStreet(),
                domain.getLocation().getHouseNumber(),
                domain.getLocation().getCity(),
                domain.getLocation().getPostalCode(),
                domain.getLocation().getState(),
                domain.getLocation().getCountry(),
                domain.getDate(),
                domain.getPrice(),
                everoutproject.Event.infrastructure.persistence.model.event.EventStatus.valueOf(domain.getStatus().name())
        );

        entity.setCancellationReason(domain.getCancellationReason());

        domain.getImages().forEach(img -> {
            EventImage imgEntity = new EventImage(img.getImageData(), entity);
            entity.addImage(imgEntity);
        });

        return entity;
    }

    public static EventImage toEntityImage(
            everoutproject.Event.domain.model.event.EventImage domainImg
    ) {
        var entity = new EventImage();
        entity.setId(domainImg.getId());
        entity.setImageData(domainImg.getImageData());
        return entity;
    }

    public static everoutproject.Event.infrastructure.persistence.model.event.EventStatus toEntityStatus(
            EventStatus domainStatus) {
        return everoutproject.Event.infrastructure.persistence.model.event.EventStatus.valueOf(domainStatus.name());
    }

    public static EventStatus toDomainStatus(
            everoutproject.Event.infrastructure.persistence.model.event.EventStatus jpaStatus) {
        return EventStatus.valueOf(jpaStatus.name());
    }
}
