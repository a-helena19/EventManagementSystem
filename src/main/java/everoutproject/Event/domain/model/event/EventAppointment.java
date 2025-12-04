package everoutproject.Event.domain.model.event;

import java.time.LocalDate;

public class EventAppointment {
    private final Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean seasonal;

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

    public void setStartDate(LocalDate startDate) {this.startDate = startDate;}
    public void setEndDate(LocalDate endDate) {this.endDate = endDate;}
    public void setSeasonal(boolean seasonal) {this.seasonal = seasonal;}
}
