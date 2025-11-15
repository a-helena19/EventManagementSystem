package at.fhv.Event.infrastructure.mapper;

import at.fhv.Event.domain.model.event.Event;
import at.fhv.Event.domain.model.event.EventLocation;
import at.fhv.Event.domain.model.event.EventStatus;
import at.fhv.Event.infrastructure.persistence.model.event.EventImage;

import java.util.List;

public class EventMapper {
    public static Event toDomain(at.fhv.Event.infrastructure.persistence.model.event.Event entity) {
        List<at.fhv.Event.domain.model.event.EventImage> images = entity.getImages().stream()
                .map(img -> new at.fhv.Event.domain.model.event.EventImage(img.getId(), img.getImageData()))
                .toList();
        EventLocation location = new EventLocation(entity.getStreet(), entity.getHouseNumber(), entity.getCity(),
                entity.getPostalCode(), entity.getState(), entity.getCountry());

        return new at.fhv.Event.domain.model.event.Event(
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

    public static at.fhv.Event.infrastructure.persistence.model.event.Event toEntity(Event domain) {

        at.fhv.Event.infrastructure.persistence.model.event.Event entity = new at.fhv.Event.infrastructure.persistence.model.event.Event(
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
                at.fhv.Event.infrastructure.persistence.model.event.EventStatus.valueOf(domain.getStatus().name())
        );

        entity.setCancellationReason(domain.getCancellationReason());

        domain.getImages().forEach(img -> {
            EventImage imgEntity = new EventImage(img.getImageData(), entity);
            entity.addImage(imgEntity);
        });

        return entity;
    }

    public static EventImage toEntityImage(
            at.fhv.Event.domain.model.event.EventImage domainImg
    ) {
        var entity = new EventImage();
        entity.setId(domainImg.getId());
        entity.setImageData(domainImg.getImageData());
        return entity;
    }

    public static at.fhv.Event.infrastructure.persistence.model.event.EventStatus toEntityStatus(
            at.fhv.Event.domain.model.event.EventStatus domainStatus) {
        return at.fhv.Event.infrastructure.persistence.model.event.EventStatus.valueOf(domainStatus.name());
    }

    public static at.fhv.Event.domain.model.event.EventStatus toDomainStatus(
            at.fhv.Event.infrastructure.persistence.model.event.EventStatus jpaStatus) {
        return at.fhv.Event.domain.model.event.EventStatus.valueOf(jpaStatus.name());
    }
}
