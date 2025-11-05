package at.fhv.Authors.domain.model;

import jakarta.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "event_images")
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @ManyToOne(fetch = FetchType.LAZY)  //many-to-one relationship, LAZY: load parent only when accessed
    @JoinColumn(name = "event_id")
    private Event event;

    public EventImage() {}

    public EventImage(byte[] imageData, Event event) {
        this.imageData = imageData;
        this.event = event;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

}
