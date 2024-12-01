package meetup.event.model.teammember;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Team member's role enum")
public enum TeamMemberRole {
    @Schema(description = "User is a team member")
    MEMBER,
    @Schema(description = "User is team's manager")
    MANAGER
}
