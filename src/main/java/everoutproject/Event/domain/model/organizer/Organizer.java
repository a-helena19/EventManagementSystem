package everoutproject.Event.domain.model.organizer;

public class Organizer {
    private final Long id;
    private final String name;
    private final String contactEmail;
    private final String phone;

    public Organizer(Long id, String name, String contactEmail, String phone) {
        this.id = id;
        this.name = name;
        this.contactEmail = contactEmail;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContactEmail() { return contactEmail; }
    public String getPhone() { return phone; }
}
