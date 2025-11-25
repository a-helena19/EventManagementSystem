package everoutproject.Event.domain.model.booking;

public class BookingAddress {

    private String street;
    private String houseNumber;
    private String city;
    private String postalCode;


    public BookingAddress(String street, String houseNumber, String city, String postalCode) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
    }


    public String getStreet() {return street;}
    public String getHouseNumber() {return houseNumber;}
    public String getCity() {return city;}
    public String getPostalCode() {return postalCode;}

    @Override
    public String toString() {
        return "address=" + street + " " + houseNumber + ", " + postalCode + " " + city;
    }


}
