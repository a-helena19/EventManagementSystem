package everoutproject.Event.infrastructure.persistence.model.booking;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;



@Entity
@Table(name = "Booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    @PastOrPresent(message = "Birth date cannot be in the future")
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = true)
    private String street;

    @Column(nullable = false)
    private String houseNumber;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = true)
    @Pattern(regexp = "^\\+[0-9 ]{6,20}$")
    private String phoneNumber;

    @Column(nullable = false)
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    // Just store the event_id as a foreign key
    @Column(name = "event_id", nullable = false)
    private Long eventId;


    // Default constructor required by JPA
    public Booking() {}

    // Constructor without id (auto-generated)
    public Booking(String firstname,
                 String lastname,
                 LocalDate birthDate,
                 LocalDate bookingDate,
                 String street,
                 String houseNumber,
                 String city,
                 String postalCode,
                 String phoneNumber,
                 String email,
                 BookingStatus status,
                   Long eventId) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthDate = birthDate;
        this.bookingDate = bookingDate;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
        this.eventId = eventId;
    }


    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {this.id = id;}

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {return lastname;}

    public void setLastname(String lastname) {this.lastname = lastname;}

    public LocalDate getBirthDate() {return birthDate;}

    public void setBirthDate(LocalDate birthDate) {this.birthDate = birthDate;}

    public LocalDate getBookingDate() {return bookingDate;}

    public void setBookingDate(LocalDate bookingDate){this.bookingDate = bookingDate;}

    public String getStreet() {return street;}

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {return houseNumber;}

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }
    public String getCity() {return city;}

    public void setCity(String city) {
        this.city = city;
    }
    public String getPostalCode() {return postalCode;}

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {return phoneNumber;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    public String getEmail() {return email;}

    public void setEmail(String email){this.email = email;}

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Long getEventId() {return eventId;}

    public void setEventId(Long eventId) {this.eventId = eventId;}


    @Override
    public String toString() {
        return "Booking [id=" + id + ", full name=" + firstname + " " + lastname + ", birth date=" + birthDate + ", booking date=" + bookingDate
                + ", address=" + street + " " + houseNumber + ", " + postalCode + " " + city + ", phone number=" + phoneNumber +
                ", email=" + email + ", booking status=" + status + ", event id=" + eventId + "]";
    }
}

