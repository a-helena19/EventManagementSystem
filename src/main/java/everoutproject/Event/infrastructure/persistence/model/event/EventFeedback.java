package everoutproject.Event.infrastructure.persistence.model.event;

import jakarta.persistence.*;

@Entity
@Table(name = "event_feedback")
public class EventFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rating; // 1–5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public EventFeedback() {}

    public EventFeedback(Integer rating, String comment, Event event) {
        this.rating = rating;
        this.comment = comment;
        this.event = event;
    }

    public Long getId() { return id; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Event getEvent() { return event; }

    public void setId(Long id) { this.id = id; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setEvent(Event event) { this.event = event; }
}
