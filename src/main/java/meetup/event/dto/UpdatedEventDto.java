package meetup.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import lombok.Builder;
import meetup.location.LocationDto;

import java.time.LocalDateTime;

@Builder
public record UpdatedEventDto(
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        LocalDateTime startDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        LocalDateTime endDateTime,
        LocationDto location
) {
}