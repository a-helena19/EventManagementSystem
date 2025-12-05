package everoutproject.Event.rest.dtos.event.response;

public record EventFeedbackDTO(
        Long id,
        Integer rating,
        String comment
) {}
