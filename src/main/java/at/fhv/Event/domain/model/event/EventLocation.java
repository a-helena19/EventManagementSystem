package at.fhv.Event.domain.model.event;

public class EventLocation {
    private String street;
    private String houseNumber;
    private String city;
    private String postalCode;
    private String state;
    private String country;

    public EventLocation(String street, String houseNumber, String city, String postalCode, String state, String country) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.state = state;
        this.country = country;
    }


    public String getStreet() {return street;}
    public String getHouseNumber() {return houseNumber;}
    public String getCity() {return city;}
    public String getPostalCode() {return postalCode;}
    public String getState() {return state;}
    public String getCountry() {return country;}


    @Override
    public String toString() {
        return "location=" + street + " "
                + houseNumber + ", " + postalCode + " " + city +
                ", " + state + ", " + country;
    }
}
