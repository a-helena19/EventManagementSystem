package everoutproject.Event.domain.model.event;

public class EventImage {
    private final Long id;
    private final byte[] imageData;

    public EventImage(Long id, byte[] imageData) {
        this.id = id;
        this.imageData = imageData;
    }

    public Long getId() {
        return id;
    }

    public byte[] getImageData() {
        return imageData;
    }
}
