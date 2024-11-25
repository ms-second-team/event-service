package meetup.event.dto.teammember;

import meetup.event.model.teammember.TeamMemberRole;

public record TeamMemberDto(
        Long eventId,
        Long userId,
        TeamMemberRole role
) {
}
