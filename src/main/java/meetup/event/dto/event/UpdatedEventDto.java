package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
public record UpdatedEventDto(
        @Pattern(regexp = "^(?!\\s*$).+", message = "Name can not be empty")
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "Start date and time must be in future")
        LocalDateTime startDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "End date and time must be in future")
        LocalDateTime endDateTime,
        @Pattern(regexp = "^(?!\\s*$).+", message = "Location can not be empty")
        String location
) {
}