package meetup.event.dto;

import jakarta.validation.constraints.NotNull;
import meetup.event.model.TeamMemberRole;

public record NewTeamMemberDto(
        @NotNull(message = "Event ID cannot be null")
        Long eventId,
        @NotNull(message = "User ID cannot be null")
        Long userId,
        @NotNull(message = "User Role cannot be null")
        TeamMemberRole role
) {
}
