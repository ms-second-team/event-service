package meetup.event.dto.teammember;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import meetup.event.model.teammember.TeamMemberRole;

public record NewTeamMemberDto(
        @Schema(description = "Event id")
        @NotNull(message = "Event ID cannot be null")
        Long eventId,
        @Schema(description = "Team member's user id")
        @NotNull(message = "User ID cannot be null")
        Long userId,
        @Schema(description = "Team member's role")
        @NotNull(message = "User Role cannot be null")
        TeamMemberRole role
) {
}