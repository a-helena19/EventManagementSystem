package everoutproject.Event.domain.model.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private final Long id;
    private String name;
    private String description;
    private EventLocation location;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<EventAppointment> appointments = new ArrayList<>();

    private BigDecimal price;
    private EventStatus status;
    private String cancellationReason;

    private Integer minParticipants;
    private Integer maxParticipants;

    private List<Requirement> requirements = new ArrayList<>();
    private List<EventEquipment> equipments = new ArrayList<>();
    private List<AdditionalPackage> additionalPackages = new ArrayList<>();

    private EventCategory category;
    private List<EventFeedback> feedback = new ArrayList<>();
    private Organizer organizer;
    private Integer durationInDays;

    private List<EventImage> images = new ArrayList<>();


    // Constructor for new event (no ID yet)
    public Event(String name,
                 String description,
                 EventLocation location,
                 LocalDate startDate,
                 LocalDate endDate,
                 BigDecimal price,
                 EventStatus status,
                 EventCategory category,
                 Organizer organizer) {

        this(
                null,
                name,
                description,
                location,
                startDate,
                endDate,
                new ArrayList<>(),
                price,
                status,
                null,
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                category,
                new ArrayList<>(),
                organizer,
                null
        );
    }

    // Constructor for reconstruction (e.g. from persistence)
    public Event(Long id,
                 String name,
                 String description,
                 EventLocation location,
                 LocalDate startDate,
                 LocalDate endDate,
                 List<EventAppointment> appointments,
                 BigDecimal price,
                 EventStatus status,
                 String cancellationReason,
                 Integer minParticipants,
                 Integer maxParticipants,
                 List<Requirement> requirements,
                 List<EventEquipment> equipment,
                 EventCategory category,
                 List<AdditionalPackage> additionalPackages,
                 Organizer organizer,
                 Integer durationInDays) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        if (appointments != null) this.appointments.addAll(appointments);
        this.price = price;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        if (requirements != null) this.requirements.addAll(requirements);
        if (equipment != null) this.equipments.addAll(equipment);
        this.category = category;
        if (additionalPackages != null) this.additionalPackages.addAll(additionalPackages);
        this.organizer = organizer;
        this.durationInDays = durationInDays;
    }

    // getters / setters (only important ones shown; add more as needed)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventLocation getLocation() { return location; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<EventAppointment> getAppointments() { return Collections.unmodifiableList(appointments); }
    public BigDecimal getPrice() { return price; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public String getCancellationReason() { return cancellationReason; }
    public Integer getMinParticipants() { return minParticipants; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public List<Requirement> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<EventEquipment> getEquipment() { return Collections.unmodifiableList(equipments); }
    public List<AdditionalPackage> getAdditionalPackages() { return Collections.unmodifiableList(additionalPackages); }
    public EventCategory getCategory() { return category; }
    public List<EventFeedback> getFeedback() { return Collections.unmodifiableList(feedback); }
    public Organizer getOrganizer() { return organizer; }
    public Integer getDurationInDays() { return durationInDays; }

    public List<EventImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    // Domain behavior (example)
    public void cancel(String reason) {
        this.status = EventStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void edit(String name,
                     String description,
                     EventLocation location,
                     LocalDate startDate,
                     LocalDate endDate,
                     List<EventAppointment> appointments,
                     BigDecimal price,
                     EventStatus status,
                     Integer minParticipants,
                     Integer maxParticipants,
                     List<Requirement> requirements,
                     List<EventEquipment> equipments,
                     EventCategory category,
                     List<AdditionalPackage> additionalPackages,
                     Organizer organizer,
                     Integer durationInDays,
                     List<EventImage> images) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        if (appointments != null) this.appointments.addAll(appointments);
        this.price = price;
        this.status = status;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        if (requirements != null) this.requirements.addAll(requirements);
        if (equipments != null) this.equipments.addAll(equipments);
        if (additionalPackages != null) this.additionalPackages.addAll(additionalPackages);
        this.category = category;
        this.organizer = organizer;
        this.durationInDays = durationInDays;
        if (images != null) this.images.addAll(images);
    }

    public void addAppointment(EventAppointment ap) {
        this.appointments.add(ap);
    }

    public void addRequirement(Requirement req) {
        this.requirements.add(req);
    }

    public void addEquipment(EventEquipment eq) {
        this.equipments.add(eq);
    }

    public void addPackage(AdditionalPackage pack) {
        this.additionalPackages.add(pack);
    }

    public void addFeedback(EventFeedback fb) {
        this.feedback.add(fb);
    }

    public void removeAppointment(EventAppointment ap) {
        this.appointments.remove(ap);
    }

    public void removeRequirement(Requirement req) {
        this.requirements.remove(req);
    }

    public void removeEquipment(EventEquipment eq) {
        this.equipments.remove(eq);
    }

    public void removePackage(AdditionalPackage pack) {
        this.additionalPackages.remove(pack);
    }

    public void removeFeedback(EventFeedback fb) {
        this.feedback.remove(fb);
    }

    public void addImage(EventImage image) {
        images.add(image);
    }

    public void removeImage(EventImage image) {
        images.remove(image);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Event");
        str.append(" [id=").append(id);
        str.append(", name=").append(name);
        str.append(", description=").append(description);
        str.append(location.toString());
        str.append(", start-date=").append(startDate);
        str.append(", end-date=").append(endDate);

        str.append(", appointments=");
        for (EventAppointment a: appointments) {
            str.append("[").append(a.toString()).append("], ");
        }
        str.append("price=").append(price);
        str.append(", status=").append(status);
        str.append(", cancellationReason=").append(cancellationReason);
        str.append(", min-participants=").append(minParticipants);
        str.append(", max-participants=").append(maxParticipants);

        str.append(", requirements=");
        for (Requirement e: requirements) {
            str.append("[").append(e.toString()).append("], ");
        }

        str.append("equipments=");
        for (EventEquipment e: equipments) {
            str.append("[").append(e.toString()).append("], ");
        }

        str.append("additional-packages=");
        for (AdditionalPackage p: additionalPackages) {
            str.append("[").append(p.toString()).append("], ");
        }

        str.append("ctegory=").append(category.toString());

        str.append("feedback=");
        for (EventFeedback f: feedback) {
            str.append("[").append(f.toString()).append("], ");
        }

        str.append("organizer=").append(organizer.toString());
        str.append(", duration-in-days").append(durationInDays);

        return str.toString();
    }
}
