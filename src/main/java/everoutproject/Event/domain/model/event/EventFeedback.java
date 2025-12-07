package everoutproject.Event.domain.model.event;

public class EventFeedback {
    private final Long id;
    private final Integer rating; // 1..5
    private final String comment;

    public EventFeedback(Long id, Integer rating, String comment) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
}
