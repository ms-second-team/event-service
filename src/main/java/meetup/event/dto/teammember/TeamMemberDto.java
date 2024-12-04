package meetup.event.dto.teammember;

import io.swagger.v3.oas.annotations.media.Schema;
import meetup.event.model.teammember.TeamMemberRole;

public record TeamMemberDto(
        @Schema(description = "Event id")
        Long eventId,
        @Schema(description = "Team member's user id")
        Long userId,
        @Schema(description = "Team member's role")
        TeamMemberRole role
) {
}
