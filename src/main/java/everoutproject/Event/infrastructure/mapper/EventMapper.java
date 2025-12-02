package everoutproject.Event.infrastructure.mapper;

import everoutproject.Event.domain.model.event.*;
import everoutproject.Event.domain.model.event.AdditionalPackage;
import everoutproject.Event.domain.model.event.EventAppointment;
import everoutproject.Event.domain.model.event.EventEquipment;
import everoutproject.Event.domain.model.event.EventFeedback;
import everoutproject.Event.domain.model.event.EventStatus;
import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.domain.model.event.Requirement;
import everoutproject.Event.domain.model.event.EventCategory;
import everoutproject.Event.infrastructure.persistence.model.event.EventImage;

import java.util.List;
import java.util.stream.Collectors;

public class EventMapper {

    //Entity -> Domain
    public static everoutproject.Event.domain.model.event.Event toDomain(everoutproject.Event.infrastructure.persistence.model.event.Event entity) {
        // map images
        List<everoutproject.Event.domain.model.event.EventImage> images = entity.getImages().stream()
                .map(img -> new everoutproject.Event.domain.model.event.EventImage(img.getId(), img.getImageData()))
                .toList();

        // location
        EventLocation location = new EventLocation(entity.getStreet(), entity.getHouseNumber(), entity.getCity(),
                entity.getPostalCode(), entity.getState(), entity.getCountry());

        // appointments
        List<EventAppointment> appointments = entity.getAppointments().stream()
                .map(a -> new EventAppointment(a.getId(), a.getStartDate(), a.getEndDate(), a.isSeasonal()))
                .toList();

        // requirements
        List<Requirement> requirements = entity.getRequirements().stream()
                .map(r -> new Requirement(r.getId(), r.getDescription()))
                .toList();

        // equipment
        List<EventEquipment> equipment = entity.getEquipments().stream()
                .map(e -> new EventEquipment(e.getId(), e.getName(), e.isRentable()))
                .collect(Collectors.toList());

        // packages
        List<AdditionalPackage> packs = entity.getAdditionalPackages().stream()
                .map(p -> new AdditionalPackage(p.getId(), p.getTitle(), p.getDescription(), p.getPrice()))
                .collect(Collectors.toList());

        // feedback
        List<EventFeedback> feedback = entity.getFeedback().stream()
                .map(f -> new EventFeedback(f.getId(), f.getRating(), f.getComment()))
                .collect(Collectors.toList());

        Organizer organizer = null;
        if (entity.getOrganizer() != null) {
            everoutproject.Event.infrastructure.persistence.model.organizer.Organizer o = entity.getOrganizer();
            organizer = new Organizer(o.getId(), o.getName(), o.getContactEmail(), o.getPhone());
        }

        everoutproject.Event.domain.model.event.Event domain = new everoutproject.Event.domain.model.event.Event(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                location,
                entity.getStartDate(),
                entity.getEndDate(),
                appointments,
                entity.getPrice(),
                entity.getDepositPercent(),
                EventStatus.valueOf(entity.getStatus().name()),
                entity.getCancellationReason(),
                entity.getMinParticipants(),
                entity.getMaxParticipants(),
                requirements,
                equipment,
                EventCategory.valueOf(entity.getCategory().name()),
                packs,
                organizer,
                entity.getDurationInDays()
        );

        images.forEach(domain::addImage);
        feedback.forEach(domain::addFeedback);
        return domain;

    }

    // Domain -> Entity
    public static everoutproject.Event.infrastructure.persistence.model.event.Event toEntity(everoutproject.Event.domain.model.event.Event domain) {

        everoutproject.Event.infrastructure.persistence.model.event.Event entity = new everoutproject.Event.infrastructure.persistence.model.event.Event(
                domain.getName(),
                domain.getDescription(),
                domain.getLocation().getStreet(),
                domain.getLocation().getHouseNumber(),
                domain.getLocation().getCity(),
                domain.getLocation().getPostalCode(),
                domain.getLocation().getState(),
                domain.getLocation().getCountry(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getPrice(),
                domain.getDepositPercent(),
                everoutproject.Event.infrastructure.persistence.model.event.EventStatus.valueOf(domain.getStatus().name()),
                everoutproject.Event.infrastructure.persistence.model.event.EventCategory.valueOf(domain.getCategory().name())
        );

        entity.setDurationInDays(domain.getDurationInDays());
        entity.setMinParticipants(domain.getMinParticipants());
        entity.setMaxParticipants(domain.getMaxParticipants());
        entity.setCancellationReason(domain.getCancellationReason());

        // organizer
        if (domain.getOrganizer() != null) {
            everoutproject.Event.infrastructure.persistence.model.organizer.Organizer org = new everoutproject.Event.infrastructure.persistence.model.organizer.Organizer();
            org.setId(domain.getOrganizer().getId());
            org.setName(domain.getOrganizer().getName());
            org.setContactEmail(domain.getOrganizer().getContactEmail());
            org.setPhone(domain.getOrganizer().getPhone());
            entity.setOrganizer(org);
        }

        // appointments
        domain.getAppointments().forEach(a -> {
            everoutproject.Event.infrastructure.persistence.model.event.EventAppointment app = new everoutproject.Event.infrastructure.persistence.model.event.EventAppointment();
            app.setStartDate(a.getStartDate());
            app.setEndDate(a.getEndDate());
            app.setSeasonal(a.isSeasonal());
            entity.addAppointment(app);
        });

        // requirements
        domain.getRequirements().forEach(r -> {
            everoutproject.Event.infrastructure.persistence.model.event.Requirement rr = new everoutproject.Event.infrastructure.persistence.model.event.Requirement();
            rr.setDescription(r.getDescription());
            entity.getRequirements().add(rr);
            rr.setEvent(entity);
        });

        // equipment
        domain.getEquipment().forEach(e -> {
            everoutproject.Event.infrastructure.persistence.model.event.EventEquipment ee = new everoutproject.Event.infrastructure.persistence.model.event.EventEquipment();
            ee.setName(e.getName());
            ee.setRentable(e.isRentable());
            entity.getEquipments().add(ee);
            ee.setEvent(entity);
        });

        // packages
        domain.getAdditionalPackages().forEach(p -> {
            everoutproject.Event.infrastructure.persistence.model.event.AdditionalPackage pkg = new everoutproject.Event.infrastructure.persistence.model.event.AdditionalPackage();
            pkg.setTitle(p.getTitle());
            pkg.setDescription(p.getDescription());
            pkg.setPrice(p.getPrice());
            entity.getAdditionalPackages().add(pkg);
            pkg.setEvent(entity);
        });

        // feedback
        domain.getFeedback().forEach(f -> {
            everoutproject.Event.infrastructure.persistence.model.event.EventFeedback ff = new everoutproject.Event.infrastructure.persistence.model.event.EventFeedback();
            ff.setRating(f.getRating());
            ff.setComment(f.getComment());
            entity.getFeedback().add(ff);
            ff.setEvent(entity);
        });

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

    public static everoutproject.Event.infrastructure.persistence.model.event.EventCategory toEntityCategory (EventCategory domainCategory) {
        return everoutproject.Event.infrastructure.persistence.model.event.EventCategory.valueOf(domainCategory.name());
    }

    public static everoutproject.Event.infrastructure.persistence.model.event.Event
    toEntityWithoutOrganizer(everoutproject.Event.domain.model.event.Event domain) {

        // Create entity WITHOUT organizer (will be injected later!)
        var entity = new everoutproject.Event.infrastructure.persistence.model.event.Event(
                domain.getName(),
                domain.getDescription(),
                domain.getLocation().getStreet(),
                domain.getLocation().getHouseNumber(),
                domain.getLocation().getCity(),
                domain.getLocation().getPostalCode(),
                domain.getLocation().getState(),
                domain.getLocation().getCountry(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getPrice(),
                domain.getDepositPercent(),
                toEntityStatus(domain.getStatus()),
                toEntityCategory(domain.getCategory())
        );

        entity.setDurationInDays(domain.getDurationInDays());
        entity.setMinParticipants(domain.getMinParticipants());
        entity.setMaxParticipants(domain.getMaxParticipants());
        entity.setCancellationReason(domain.getCancellationReason());

        // Appointments
        domain.getAppointments().forEach(a -> {
            var app = new everoutproject.Event.infrastructure.persistence.model.event.EventAppointment();
            app.setStartDate(a.getStartDate());
            app.setEndDate(a.getEndDate());
            app.setSeasonal(a.isSeasonal());
            entity.addAppointment(app);
        });

        // Requirements
        domain.getRequirements().forEach(r -> {
            var rr = new everoutproject.Event.infrastructure.persistence.model.event.Requirement();
            rr.setDescription(r.getDescription());
            entity.getRequirements().add(rr);
            rr.setEvent(entity);
        });

        // Equipment
        domain.getEquipment().forEach(e -> {
            var ee = new everoutproject.Event.infrastructure.persistence.model.event.EventEquipment();
            ee.setName(e.getName());
            ee.setRentable(e.isRentable());
            entity.getEquipments().add(ee);
            ee.setEvent(entity);
        });

        // Packages
        domain.getAdditionalPackages().forEach(p -> {
            var pkg = new everoutproject.Event.infrastructure.persistence.model.event.AdditionalPackage();
            pkg.setTitle(p.getTitle());
            pkg.setDescription(p.getDescription());
            pkg.setPrice(p.getPrice());
            entity.getAdditionalPackages().add(pkg);
            pkg.setEvent(entity);
        });

        // Images
        domain.getImages().forEach(img -> {
            everoutproject.Event.infrastructure.persistence.model.event.EventImage imgEntity = new everoutproject.Event.infrastructure.persistence.model.event.EventImage(img.getImageData(), entity);
            entity.addImage(imgEntity);
        });

        return entity;
    }
}
