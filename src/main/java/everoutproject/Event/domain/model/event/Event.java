package everoutproject.Event.domain.model.event;

import everoutproject.Event.domain.model.organizer.Organizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private Long id;
    private String name;
    private String description;
    private EventLocation location;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<EventAppointment> appointments = new ArrayList<>();

    private BigDecimal price;
    private Integer depositPercent;
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
                 Integer depositPercent,
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
                depositPercent != null ? depositPercent : 30,
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
                 Integer depositPercent,
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
        this.depositPercent = depositPercent;
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
    public void setId(Long id) {this.id = id;}
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventLocation getLocation() { return location; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<EventAppointment> getAppointments() { return Collections.unmodifiableList(appointments); }
    public BigDecimal getPrice() { return price; }
    public Integer getDepositPercent() { return depositPercent; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public String getCancellationReason() { return cancellationReason; }
    public Integer getMinParticipants() { return minParticipants; }
    public void setMinParticipants(Integer minParticipants) {this.minParticipants = minParticipants;}
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) {this.maxParticipants = maxParticipants;}
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
                     Integer depositPercent,
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
        if (appointments != null) {
            this.appointments.clear();
            this.appointments.addAll(appointments);
        }
        this.price = price;
        this.depositPercent = depositPercent;
        this.status = status;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        if (requirements != null) {
            this.requirements.clear();
            this.requirements.addAll(requirements);
        }
        if (equipments != null) {
            this.equipments.clear();
            this.equipments.addAll(equipments);
        }
        if (additionalPackages != null) {
            this.additionalPackages.clear();
            this.additionalPackages.addAll(additionalPackages);
        }
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

    public void calculateDuration() {
        if (startDate == null) {
            this.durationInDays = null;
            return;
        }

        // Single-day event
        if (endDate == null) {
            this.durationInDays = 1;
            return;
        }

        // Multi-day event
        this.durationInDays = (int) (java.time.Duration.between(
                startDate.atStartOfDay(),
                endDate.atStartOfDay()
        ).toDays() + 1);
    }


    @Override
    public String toString() {
        return "Event [id=" + id +
                ", name=" + name +
                ", depositPercent=" + depositPercent +
                ", price=" + price +
                ", start=" + startDate +
                ", end=" + endDate +
                "]";
    }
}
