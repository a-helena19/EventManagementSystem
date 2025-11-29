package everoutproject.Event.domain.model.event;

public class EventEquipment {
    private final Long id;
    private final String name;
    private final boolean rentable; // whether participants can rent it

    public EventEquipment(Long id, String name, boolean rentable) {
        this.id = id;
        this.name = name;
        this.rentable = rentable;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isRentable() { return rentable; }
}
