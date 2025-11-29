package everoutproject.Event.infrastructure.persistence.model.event;

import jakarta.persistence.*;

@Entity
@Table(name = "event_equipment")
public class EventEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean rentable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public EventEquipment() {}

    public EventEquipment(String name, boolean rentable, Event event) {
        this.name = name;
        this.rentable = rentable;
        this.event = event;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isRentable() { return rentable; }
    public Event getEvent() { return event; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRentable(boolean rentable) { this.rentable = rentable; }
    public void setEvent(Event event) { this.event = event; }
}
