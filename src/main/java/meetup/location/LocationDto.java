package meetup.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LocationDto(
        @NotBlank(message = "Name can not be blank")
        String name,
        @NotNull(message = "Latitude cannot be null")
        Float lat,
        @NotNull(message = "Longitude cannot be null")
        Float lon
) {
}