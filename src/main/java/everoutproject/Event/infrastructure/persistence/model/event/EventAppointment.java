package everoutproject.Event.infrastructure.persistence.model.event;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "event_appointments")
public class EventAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_date", nullable = false)
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(name="seasonal")
    private boolean seasonal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;


    public EventAppointment() {}
    public EventAppointment(LocalDate startDate, LocalDate endDate, boolean recurring) {
        this.startDate = startDate; this.endDate = endDate; this.seasonal = recurring;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isSeasonal() { return seasonal; }
    public void setSeasonal(boolean seasonal) {this.seasonal = seasonal;}
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
