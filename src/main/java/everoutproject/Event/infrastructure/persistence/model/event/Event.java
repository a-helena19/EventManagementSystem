package everoutproject.Event.infrastructure.persistence.model.event;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    // location fields
    @Column(nullable = false)
    private String street;
    @Column(nullable = false)
    private String houseNumber;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String postalCode;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String country;


    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventAppointment> appointments = new ArrayList<>();

    // using up to 10 digits total, inclusive 2 digits after comma (e.g. 99999999.99)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "cancel_reason",nullable = true)
    private String cancellationReason;

    @Column(name = "min_participants")
    private Integer minParticipants;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    // Relationships to supporting entities
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Requirement> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventEquipment> equipments = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdditionalPackage> additionalPackages = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventFeedback> feedback = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @Column(name = "duration_days")
    private Integer durationInDays;

    // mappedBy: bidirectional relationship and event is the owner.
    // CascadeType.ALL: all operations (persist, merge, remove, refresh, detach) on the parent (event) will be automatically applied on the child (eventImage)
    // orphanRemoval = true: removing an eventImage from images -> will be automatically removed from the database
    // EAGER: Loads all images every time you load events, even if you don’t need them
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventImage> images = new ArrayList<>();

    // Default constructor required by JPA
    public Event() {}

    // Constructor without id (auto-generated)
    public Event(String name,
                 String description,
                 String street,
                 String houseNumber,
                 String city,
                 String postalCode,
                 String state,
                 String country,
                 LocalDate startDate,
                 LocalDate endDate,
                 BigDecimal price,
                 EventStatus status,
                 EventCategory category) {
        this.name = name;
        this.description = description;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.status = status;
        this.category = category;
    }


    // Getter and Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStreet() {return street;}
    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {return houseNumber;}
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getCity() {return city;}
    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getState() {return state;}
    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {return country;}
    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<EventAppointment> getAppointments() { return appointments; }
    public void setAppointments(List<EventAppointment> appointments) {
        this.appointments = appointments;
        for (EventAppointment a : appointments) a.setEvent(this);
    }
    public void addAppointment(EventAppointment a) {
        appointments.add(a); a.setEvent(this);
    }

    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public EventStatus getStatus() {
        return status;
    }
    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getCancellationReason(){
        return cancellationReason;
    }
    public void setCancellationReason(String reason){
        this.cancellationReason = reason;
    }

    public Integer getMinParticipants() { return minParticipants; }
    public void setMinParticipants(Integer minParticipants) { this.minParticipants = minParticipants; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public List<Requirement> getRequirements() { return requirements; }
    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
        for (Requirement r : requirements) r.setEvent(this);
    }

    public List<EventEquipment> getEquipments() { return equipments; }
    public void setEquipments(List<EventEquipment> equipment) {
        this.equipments = equipment;
        for (EventEquipment e : equipment) e.setEvent(this);
    }

    public List<AdditionalPackage> getAdditionalPackages() { return additionalPackages; }
    public void setAdditionalPackages(List<AdditionalPackage> additionalPackages) {
        this.additionalPackages = additionalPackages;
        for (AdditionalPackage p : additionalPackages) p.setEvent(this);
    }

    public List<EventFeedback> getFeedback() { return feedback; }
    public void setFeedback(List<EventFeedback> feedback) {
        this.feedback = feedback;
        for (EventFeedback f : feedback) f.setEvent(this);
    }

    public Organizer getOrganizer() { return organizer; }
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public Integer getDurationInDays() { return durationInDays; }
    public void setDurationInDays(Integer durationInDays) { this.durationInDays = durationInDays; }

    public List<EventImage> getImages() {
        return images;
    }
    public void setImages(List<EventImage> images) {
        this.images = images;
        for (EventImage img : images) {
            img.setEvent(this);
        }
    }

    public void addImage(EventImage image) {
        images.add(image);
        image.setEvent(this);
    }
    public void removeImage(EventImage image) {
        images.remove(image);
        image.setEvent(null);
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder("Event");
        str.append(" [id=").append(id);
        str.append(", name=").append(name);
        str.append(", description=").append(description);
        str.append(", location=" + street + " " + houseNumber + ", " + postalCode + " " + city + ", " + state + ", " + country);
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

        str.append("category=").append(category.toString());

        str.append("feedback=");
        for (EventFeedback f: feedback) {
            str.append("[").append(f.toString()).append("], ");
        }

        str.append("organizer=").append(organizer.toString());
        str.append(", duration-in-days").append(durationInDays);

        return str.toString();

    }
}

