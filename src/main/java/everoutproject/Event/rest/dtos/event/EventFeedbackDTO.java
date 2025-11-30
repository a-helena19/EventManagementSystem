package everoutproject.Event.rest.dtos.event;

public record EventFeedbackDTO(
        Long id,
        Integer rating,
        String comment
) {}
