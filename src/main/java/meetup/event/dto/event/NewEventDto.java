package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
public record NewEventDto(
        @NotBlank(message = "Name can not be blank")
        String name,
        @NotBlank(message = "Description can not be blank")
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "Start date and time must be in future")
        @NotNull(message = "Start date and time cannot be null")
        LocalDateTime startDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "End date and time must be in future")
        @NotNull(message = "End date and time cannot be null")
        LocalDateTime endDateTime,
        @NotBlank(message = "Location cannot be null")
        String location
) {
}