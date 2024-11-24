package meetup.event.service;

import meetup.event.dto.NewTeamMemberDto;
import meetup.event.dto.TeamMemberDto;
import meetup.event.dto.UpdateTeamMemberDto;

import java.util.List;

public interface TeamMemberService {
    TeamMemberDto addTeamMember(Long userId, NewTeamMemberDto newTeamMemberDto);

    List<TeamMemberDto> getTeamsByEventId(Long userId, Long eventId);

    TeamMemberDto updateTeamMemberInEvent(Long userId, Long eventId, Long memberId, UpdateTeamMemberDto updateTeamMemberDto);

    void deleteTeamMemberFromEvent(Long userId, Long eventId, Long memberId);
}
