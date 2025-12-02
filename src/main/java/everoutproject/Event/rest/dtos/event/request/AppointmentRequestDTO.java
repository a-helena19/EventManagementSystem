package everoutproject.Event.rest.dtos.event.request;

import java.time.LocalDate;

public class AppointmentRequestDTO {
    public Long id;
    public LocalDate startDate;
    public LocalDate endDate;
    public boolean seasonal;
}
