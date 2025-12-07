package everoutproject.Event.rest.dtos.booking;

public class CancelBookingRequestDTO {
    private String reason;

    public CancelBookingRequestDTO() { }

    public CancelBookingRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

