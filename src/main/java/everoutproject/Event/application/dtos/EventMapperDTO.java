package everoutproject.Event.application.dtos;

import everoutproject.Event.domain.model.event.*;
import everoutproject.Event.domain.model.organizer.Organizer;
import everoutproject.Event.rest.dtos.event.response.*;

public class EventMapperDTO {

    public static EventDTO toDTO(Event event) {
        return new EventDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                toLocationDTO(event.getLocation()),  // structured location

                event.getStartDate(),
                event.getEndDate(),

                event.getAppointments().stream().map(EventMapperDTO::toAppointmentDTO).toList(),

                event.getPrice(),
                event.getDepositPercent(),
                event.getStatus().name(),
                event.getCancellationReason(),

                event.getMinParticipants(),
                event.getMaxParticipants(),

                event.getRequirements().stream().map(EventMapperDTO::toRequirementDTO).toList(),
                event.getEquipment().stream().map(EventMapperDTO::toEquipmentDTO).toList(),
                event.getAdditionalPackages().stream().map(EventMapperDTO::toAdditionalPackageDTO).toList(),

                event.getCategory().name(),
                toOrganizerDTO(event.getOrganizer()),
                event.getDurationInDays(),

                event.getFeedback().stream().map(EventMapperDTO::toFeedbackDTO).toList(),

                event.getImages().stream().map(EventImage::getId).toList()
        );
    }

    private static EventLocationDTO toLocationDTO(EventLocation location) {
        if (location == null) return null;
        return new EventLocationDTO(
                location.getStreet(),
                location.getHouseNumber(),
                location.getCity(),
                location.getPostalCode(),
                location.getState(),
                location.getCountry()
        );
    }

    private static EventAppointmentDTO toAppointmentDTO(EventAppointment a) {
        return new EventAppointmentDTO(
                a.getId(),
                a.getStartDate(),
                a.getEndDate(),
                a.isSeasonal()
        );
    }

    private static RequirementDTO toRequirementDTO(Requirement r) {
        return new RequirementDTO(
                r.getId(),
                r.getDescription()
        );
    }

    private static EventEquipmentDTO toEquipmentDTO(EventEquipment e) {
        return new EventEquipmentDTO(
                e.getId(),
                e.getName(),
                e.isRentable()
        );
    }

    private static AdditionalPackageDTO toAdditionalPackageDTO(AdditionalPackage p) {
        return new AdditionalPackageDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPrice()
        );
    }

    private static EventFeedbackDTO toFeedbackDTO(EventFeedback f) {
        return new EventFeedbackDTO(
                f.getId(),
                f.getRating(),
                f.getComment()
        );
    }

    private static OrganizerDTO toOrganizerDTO(Organizer organizer) {
        if (organizer == null) return null;

        return new OrganizerDTO(
                organizer.getId(),
                organizer.getName(),
                organizer.getContactEmail(),
                organizer.getPhone()
        );
    }
}

