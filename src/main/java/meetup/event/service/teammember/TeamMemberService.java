package meetup.event.service.teammember;

import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;

import java.util.List;

public interface TeamMemberService {
    TeamMemberDto addTeamMember(Long userId, NewTeamMemberDto newTeamMemberDto);

    List<TeamMemberDto> getTeamsByEventId(Long userId, Long eventId);

    TeamMemberDto updateTeamMemberInEvent(Long userId, Long eventId, Long memberId, UpdateTeamMemberDto updateTeamMemberDto);

    void deleteTeamMemberFromEvent(Long userId, Long eventId, Long memberId);
}