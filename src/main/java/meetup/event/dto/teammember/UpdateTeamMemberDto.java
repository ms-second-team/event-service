package meetup.event.dto.teammember;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import meetup.event.model.teammember.TeamMemberRole;

public record UpdateTeamMemberDto(
        @Schema(description = "Team member's new role")
        @NotNull(message = "User Role cannot be null")
        TeamMemberRole role
) {
}
