package everoutproject.Event.infrastructure.persistence.model.event;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "additional_packages")
public class AdditionalPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public AdditionalPackage() {}

    public AdditionalPackage(String title, String description, BigDecimal price, Event event) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.event = event;
    }

    public AdditionalPackage(Long id, String title, String description, BigDecimal price, Event event) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.event = event;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Event getEvent() { return event; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setEvent(Event event) { this.event = event; }
}
