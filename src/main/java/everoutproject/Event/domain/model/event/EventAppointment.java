package everoutproject.Event.domain.model.event;

import java.time.LocalDate;

public class EventAppointment {
    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean seasonal;

    public EventAppointment(Long id, LocalDate startDate, LocalDate endDate, boolean recurring) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.seasonal = recurring;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isSeasonal() { return seasonal; }
}
