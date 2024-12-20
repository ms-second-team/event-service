package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import meetup.event.model.event.EventRegistrationStatus;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
public record NewEventDto(

        @Schema(description = "Event's name")
        @NotBlank(message = "Name can not be blank")
        String name,
        @Schema(description = "Event's description")
        @NotBlank(message = "Description can not be blank")
        String description,
        @Schema(description = "Event's start datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "Start date and time must be in future")
        @NotNull(message = "Start date and time cannot be null")
        LocalDateTime startDateTime,
        @Schema(description = "Event's end datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "End date and time must be in future")
        @NotNull(message = "End date and time cannot be null")
        LocalDateTime endDateTime,
        @Schema(description = "Event's location")
        @NotBlank(message = "Location cannot be null")
        String location,
        @Schema(description = "Event's participants limit")
        @PositiveOrZero(message = "Participant limit must be positive or zero")
        int participantLimit,
        @Schema(description = "Event's registration status")
        @NotNull(message = "Event's registration status cannot be null")
        EventRegistrationStatus registrationStatus
) {
}