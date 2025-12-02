package everoutproject.Event.domain.model.booking;

import java.time.LocalDate;

public class Booking {

    private Long id;

    private String firstname;
    private String lastname;
    private LocalDate birthDate;
    private LocalDate bookingDate;
    private BookingAddress address;
    private String phoneNumber;
    private String email;
    private BookingStatus status;
    private Long eventId;
    private Long userId;

    public Booking() {}

    public Booking(String firstname,
                   String lastname,
                   LocalDate birthDate,
                   LocalDate bookingDate,
                   BookingAddress address,
                   String phoneNumber,
                   String email,
                   BookingStatus status,
                   Long eventId,
                   Long userId) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthDate = birthDate;
        this.bookingDate = bookingDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
        this.eventId = eventId;
        this.userId = userId;
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

    public BookingAddress getAddress() {return address;}

    public void setAddress(BookingAddress address) {
        this.address = address;
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

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}


    @Override
    public String toString() {
        return "Booking [id=" + id + ", full name=" + firstname + " " + lastname + ", birth date=" + birthDate + ", booking date=" + bookingDate
                + ", " + address.toString() + " , phone number=" + phoneNumber +
                ", email=" + email + ", booking status=" + status + ", event id=" + eventId + ", user id=" + userId + "]";
    }
}

