package at.fhv.Event.domain.model.booking;

import java.time.LocalDate;

public class Booking {

    private Long id;

    private String name;
    private LocalDate birthDate;
    private LocalDate bookingDate;
    private BookingAddress address;
    private String phoneNumber;
    private String email;
    private BookingStatus status;
    private Long eventId;

    public Booking() {}

    public Booking(String name,
                   LocalDate birthDate,
                   LocalDate bookingDate,
                   BookingAddress address,
                   String phoneNumber,
                   String email,
                   BookingStatus status) {
        this.name = name;
        this.birthDate = birthDate;
        this.bookingDate = bookingDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
    }


    // Getter and Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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


    @Override
    public String toString() {
        return "Booking [id=" + id + ", name=" + name + ", birth date=" + birthDate + ", booking date=" + bookingDate
                + ", " + address.toString() + " , phone number=" + phoneNumber +
                ", email=" + email + ", booking status=" + status + ", event id=" + eventId + "]";
    }
}

