package meetup.location;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record LocationDto(
    @NotNull
    String name,
    @NotNull
    Float lat,
    @NotNull
    Float lon
){
}