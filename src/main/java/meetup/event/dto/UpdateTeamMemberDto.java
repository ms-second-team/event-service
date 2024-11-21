package meetup.event.dto;

import jakarta.validation.constraints.NotNull;
import meetup.event.model.TeamMemberRole;

public record UpdateTeamMemberDto(
        @NotNull(message = "User Role cannot be null")
        TeamMemberRole role
) {
}
