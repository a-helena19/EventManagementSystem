package everoutproject.Event.rest.dtos.booking;

import jakarta.validation.constraints.NotBlank;

public class CreateBookingWithPaymentRequestDTO extends CreateBookingRequestDTO {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
