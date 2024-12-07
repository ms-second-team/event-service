package meetup.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "User")
@Builder
public record UserDto(
        @Schema(description = "User id")
        Long id,
        @Schema(description = "User name")
        String name,
        @Schema(description = "User email")
        String email,
        @Schema(description = "User password")
        String password,
        @Schema(description = "User about me")
        String aboutMe
) {
}
