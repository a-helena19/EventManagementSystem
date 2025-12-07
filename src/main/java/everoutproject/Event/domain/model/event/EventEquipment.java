package everoutproject.Event.domain.model.event;

public class EventEquipment {
    private final Long id;
    private String name;
    private boolean rentable; // whether participants can rent it

    public EventEquipment(Long id, String name, boolean rentable) {
        this.id = id;
        this.name = name;
        this.rentable = rentable;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isRentable() { return rentable; }

    public void setName(String name) {this.name = name;}
    public void setRentable(boolean rentable) {this.rentable = rentable;}
}
