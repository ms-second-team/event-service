package meetup.event.dto;

import meetup.event.model.TeamMemberRole;

public record TeamMemberDto(
        Long eventId,
        Long userId,
        TeamMemberRole memberRole
) {
}
