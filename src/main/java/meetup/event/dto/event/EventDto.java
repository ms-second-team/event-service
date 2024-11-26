package meetup.event.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

import static meetup.Constants.DATA_PATTERN;

@Builder
public record EventDto(
        Long id,
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime createdDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime startDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_PATTERN)
        LocalDateTime endDateTime,
        String location,
        Long ownerId
) {
}