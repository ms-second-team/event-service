package meetup.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.dto.NewTeamMemberDto;
import meetup.event.dto.TeamMemberDto;
import meetup.event.dto.UpdateTeamMemberDto;
import meetup.event.mapper.TeamMemberMapper;
import meetup.event.model.Event;
import meetup.event.model.TeamMember;
import meetup.event.model.TeamMemberRole;
import meetup.event.repository.TeamMemberRepository;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final EventService eventService;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public TeamMemberDto addTeamMember(Long userId, NewTeamMemberDto newTeamMemberDto) {
        checkTeamMemberManagerOrOwnerRoleInEvent(newTeamMemberDto.eventId(), userId);
        TeamMember teamMember = teamMemberRepository.save(teamMemberMapper.toTeamMember(newTeamMemberDto));
        log.info("Member id = '{}' was added to team event id = '{}' by user id = '{}'",
                teamMember.getId().getUserId(), teamMember.getId().getEventId(), userId);
        return teamMemberMapper.toTeamMemberDto(teamMember);
    }

    @Override
    public List<TeamMemberDto> getTeamsByEventId(Long userId, Long eventId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByIdEventId(eventId);
        log.debug("Found '{}' team members by event id = '{}' by user id ='{}'", teamMembers.size(), eventId, userId);
        if (teamMembers.isEmpty()) {
            return List.of();
        }
        return teamMemberMapper.toTeamMemberDtoList(teamMembers);
    }

    @Override
    public TeamMemberDto updateTeamMemberInEvent(Long userId, Long eventId, Long memberId, UpdateTeamMemberDto updateTeamMemberDto) {
        checkTeamMemberManagerOrOwnerRoleInEvent(eventId, userId);
        TeamMember member = getTeamMember(eventId, memberId);
        teamMemberMapper.updateTeamMember(updateTeamMemberDto, member);
        TeamMember upadatedTeamMember = teamMemberRepository.save(member);
        log.info("Member with id = '{}' was updated in team event id = '{}' by user id = '{}'", memberId, eventId, userId);
        return teamMemberMapper.toTeamMemberDto(upadatedTeamMember);
    }

    @Override
    public void deleteTeamMemberFromEvent(Long userId, Long eventId, Long memberId) {
        checkTeamMemberManagerOrOwnerRoleInEvent(eventId, userId);
        TeamMember member = getTeamMember(eventId, memberId);
        teamMemberRepository.delete(member);
        log.info("Member with id = '{}' was deleted from team event id = '{}'", memberId, eventId);
    }

    private TeamMember getTeamMember(Long eventId, Long memberId) {
        return teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId).orElseThrow(
                () -> new NotFoundException(String.format("Team member id = %d in team event id = %d", memberId, eventId))
        );
    }

    private void checkTeamMemberManagerOrOwnerRoleInEvent(Long eventId, Long memberId) {
        Event event = eventService.getEventByEventId(eventId, memberId);
        if (event == null) {
            throw new NotFoundException("Event id = " + eventId + " not found!");
        } else if (!event.getOwnerId().equals(memberId)) {
            TeamMember user = getTeamMember(eventId, memberId);
            if (!user.getRole().equals(TeamMemberRole.MANAGER)) {
                throw new NotAuthorizedException(String.format("User id = %d in event id = %d not Manager", memberId, eventId));
            }
        }
    }
}
