package meetup.event.dto.teammember;

import jakarta.validation.constraints.NotNull;
import meetup.event.model.teammember.TeamMemberRole;

public record UpdateTeamMemberDto(
        @NotNull(message = "User Role cannot be null")
        TeamMemberRole role
) {
}
