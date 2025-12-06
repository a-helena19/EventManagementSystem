package everoutproject.Event.rest.dtos.event.request;

import everoutproject.Event.rest.dtos.event.response.*;
import everoutproject.Event.rest.validation.ValidDateRange;
import everoutproject.Event.rest.validation.ValidParticipantsRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;


@ValidDateRange(
        startDateField = "startDate",
        endDateField = "endDate",
        message = "End date must be after or equal to start date"
)
@ValidParticipantsRange
public class CreateEventRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    public String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    public String description;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    public LocalDate startDate;

    @FutureOrPresent(message = "End date must be in the present or future")
    public LocalDate endDate;

    @FutureOrPresent(message = "Cancel deadline must be in the present or future")
    public LocalDate cancelDeadline;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
    public Double price;

    @Min(value = 0, message = "Deposit percent cannot be negative")
    @Max(value = 100, message = "Deposit percent cannot be more than 100")
    public Integer depositPercent;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    public String category;

    @Positive(message = "Organizer ID must be positive")
    public Long organizerId;

    @Valid
    public NewOrganizerRequestDTO newOrganizer;

    @NotNull(message = "Min participants is required")
    @Min(value = 1, message = "Min participants must be at least 1")
    @Max(value = 10000, message = "Min participants cannot exceed 10000")
    public Integer minParticipants;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 10000, message = "Max participants cannot exceed 10000")
    public Integer maxParticipants;

    @Valid
    public EventLocationDTO location;

    @Valid
    @Size(max = 50, message = "Cannot have more than 50 requirements")
    public List<RequirementRequestDTO> requirements;

    @Valid
    @Size(max = 100, message = "Cannot have more than 100 equipment items")
    public List<EquipmentRequestDTO> equipment;

    @Valid
    @Size(max = 20, message = "Cannot have more than 20 additional packages")
    public List<AdditionalPackageRequestDTO> additionalPackages;

    @Valid
    @Size(max = 365, message = "Cannot have more than 365 appointments")
    public List<AppointmentRequestDTO> appointments;

    /**
     * Custom validation: Cancel deadline must be before or equal to start date.
     */
    @AssertTrue(message = "Cancel deadline must be before or equal to start date")
    public boolean isCancelDeadlineValid() {
        if (cancelDeadline == null || startDate == null) {
            return true;
        }
        return !cancelDeadline.isAfter(startDate);
    }
}
