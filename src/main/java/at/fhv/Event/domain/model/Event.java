package at.fhv.Event.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@Table(name = "Event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate date;

    // using up to 10 digits total, inclusive 2 digits after comma (e.g. 99999999.99)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "cancel_reason",nullable = true)
    private String cancellationReason;

    // mappedBy: bidirectional relationship and event is the owner.
    // CascadeType.ALL: all operations (persist, merge, remove, refresh, detach) on the parent (event) will be automatically applied on the child (eventImage)
    // orphanRemoval = true: removing an eventImage from images -> will be automatically removed from the database
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventImage> images = new ArrayList<>();

    // Default constructor required by JPA
    public Event() {}

    // Constructor without id (auto-generated)
    public Event(String name, String description, String location, LocalDate date, BigDecimal price, Status status) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.price = price;
        this.status = status;
        this.images = new ArrayList<>();

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCancellationReason(){
        return cancellationReason;
    }

    public void setCancellationReason(String reason){
        this.cancellationReason = reason;
    }

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
        return "Event [id=" + id + ", name=" + name + ", description=" + description + ", location=" + location +
                ", date=" + date + ", price=" + price + ", status=" + status + "]";
    }
}

