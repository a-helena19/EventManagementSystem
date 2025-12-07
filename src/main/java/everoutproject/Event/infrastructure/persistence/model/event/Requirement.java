package everoutproject.Event.infrastructure.persistence.model.event;

import jakarta.persistence.*;

@Entity
@Table(name = "event_requirements")
public class Requirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public Requirement() {}

    public Requirement(String description, Event event) {
        this.description = description;
        this.event = event;
    }

    public Requirement(Long id, String description, Event event) {
        this.id = id;
        this.description = description;
        this.event = event;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public Event getEvent() { return event; }

    public void setId(Long id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setEvent(Event event) { this.event = event; }
}
