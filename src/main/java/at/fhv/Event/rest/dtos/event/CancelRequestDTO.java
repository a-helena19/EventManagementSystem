package at.fhv.Event.rest.dtos.event;

public class CancelRequestDTO {
    private String reason;

    public CancelRequestDTO() { }

    public CancelRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
