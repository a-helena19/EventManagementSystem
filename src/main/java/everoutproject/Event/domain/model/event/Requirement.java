package everoutproject.Event.domain.model.event;

public class Requirement {
    private final Long id;
    private final String description;

    public Requirement(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
}
