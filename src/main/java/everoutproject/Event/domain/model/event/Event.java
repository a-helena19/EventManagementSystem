package everoutproject.Event.domain.model.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private final Long id;
    private String name;
    private String description;
    private EventLocation location;
    private LocalDate date;
    private BigDecimal price;
    private EventStatus status;
    private String cancellationReason;
    private List<EventImage> images = new ArrayList<>();


    // Constructor for new event (no ID yet)
    public Event(String name,
                 String description,
                 EventLocation location,
                 LocalDate date,
                 BigDecimal price,
                 EventStatus status) {

        this(null, name, description, location, date, price, status, null, new ArrayList<>());
    }

    // Constructor for reconstruction (e.g. from persistence)
    public Event(Long id,
                 String name,
                 String description,
                 EventLocation location,
                 LocalDate date,
                 BigDecimal price,
                 EventStatus status,
                 String cancellationReason,
                 List<EventImage> images) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.price = price;
        this.status = status;
        this.cancellationReason = cancellationReason;

        if (images != null) {
            this.images.addAll(images);
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventLocation getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public BigDecimal getPrice() { return price; }
    public EventStatus getStatus() { return status; }
    public String getCancellationReason() { return cancellationReason; }

    public List<EventImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    // Domain behavior (example)
    public void cancel(String reason) {
        this.status = EventStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void edit(String name, String description, EventLocation location, LocalDate date, BigDecimal price,
                     EventStatus status, List<EventImage> images) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.price = price;
        this.status = status;
        this.images = images;
    }

    public void addImage(EventImage image) {
        images.add(image);
    }

    public void removeImage(EventImage image) {
        images.remove(image);
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", description=" + description + ", " +
                location.toString() + " , date=" + date + ", price=" + price + ", status=" + status + "]";
    }
}
