package at.fhv.Event.infrastructure.persistence.model.event;

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

    @Column(nullable = false)
    private LocalDate date;

    // using up to 10 digits total, inclusive 2 digits after comma (e.g. 99999999.99)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "cancel_reason",nullable = true)
    private String cancellationReason;

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
                 LocalDate date,
                 BigDecimal price,
                 EventStatus status) {
        this.name = name;
        this.description = description;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.country = country;
        this.date = date;
        this.price = price;
        this.status = status;
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
        return "Event [id=" + id + ", name=" + name + ", description=" + description + ", location=" + street + " "
                + houseNumber + ", " + postalCode + " " + city + ", " + state + ", " + country +
                ", date=" + date + ", price=" + price + ", status=" + status + "]";
    }
}

