package meetup.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import meetup.location.LocationDto;

import java.time.LocalDateTime;

@Builder
public record NewEventDto(
        @NotNull
        @NotEmpty
        @NotBlank
        String name,
        @NotNull
        @NotEmpty
        @NotBlank
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        @NotNull
        LocalDateTime startDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        @NotNull
        LocalDateTime endDateTime,
        @NotNull
        LocationDto location
) {
}