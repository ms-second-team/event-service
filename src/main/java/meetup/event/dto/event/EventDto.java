package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import meetup.event.model.event.RegistrationStatus;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
public record EventDto(
        @Schema(description = "Event's id")
        Long id,
        @Schema(description = "Event's name")
        String name,
        @Schema(description = "Event's description")
        String description,
        @Schema(description = "Event's creation datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime createdDateTime,
        @Schema(description = "Event's start datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime startDateTime,
        @Schema(description = "Event's end datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime endDateTime,
        @Schema(description = "Event's location")
        String location,
        @Schema(description = "Event's owner id")
        Long ownerId,
        @Schema(description = "Event's registration status")
        RegistrationStatus registrationStatus
) {
}