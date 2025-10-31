package at.fhv.Authors.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(name = "Event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Description is optional, thus it can be null
    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate date;

    // using up to 10 digits total, inclusive 2 digits after comma (e.g. 99999999.99)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Default constructor required by JPA
    public Event() {}

    // Constructor without id (auto-generated)
    public Event(String name, String description, String location, LocalDate date, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.price = price;
    }


    @Override
    public String toString() {
        return "Event: [id= " + id + ", name= " + name + ", description= " + description + ", location= " + location + ", date= " + date + ", price= " + price + "]";
    }


    // Getter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }
}

