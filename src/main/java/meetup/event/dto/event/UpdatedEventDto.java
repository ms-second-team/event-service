package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
@Schema(description = "Event update request dto")
public record UpdatedEventDto(
        @Schema(description = "Event's name")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Name can not be empty")
        String name,
        @Schema(description = "Event's description")
        String description,
        @Schema(description = "Event's start datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "Start date and time must be in future")
        LocalDateTime startDateTime,
        @Schema(description = "Event's end datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        @Future(message = "End date and time must be in future")
        LocalDateTime endDateTime,
        @Schema(description = "Event's location")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Location can not be empty")
        String location
) {
}