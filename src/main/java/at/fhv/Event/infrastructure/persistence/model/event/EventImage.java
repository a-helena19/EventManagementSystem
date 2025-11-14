package at.fhv.Event.infrastructure.persistence.model.event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "event_images")
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @ManyToOne(fetch = FetchType.LAZY)  //many-to-one relationship, LAZY: load parent only when accessed
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    public EventImage() {}

    public EventImage(byte[] imageData, Event event) {
        this.imageData = imageData;
        this.event = event;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

}
