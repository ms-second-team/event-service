package meetup.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.client.UserClient;
import meetup.event.dto.UserDto;
import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;
import meetup.event.mapper.TeamMemberMapper;
import meetup.event.model.event.Event;
import meetup.event.model.teammember.TeamMember;
import meetup.event.model.teammember.TeamMemberId;
import meetup.event.model.teammember.TeamMemberRole;
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
    private final UserClient userClient;

    @Override
    public TeamMemberDto addTeamMember(Long userId, NewTeamMemberDto newTeamMemberDto) {
        checkUserExists(userId, userId);
        checkUserExists(userId, newTeamMemberDto.userId());
        checkTeamMemberManagerOrOwnerRoleInEvent(newTeamMemberDto.eventId(), userId);
        TeamMemberId teamMemberId = new TeamMemberId(newTeamMemberDto.eventId(), newTeamMemberDto.userId());
        TeamMember teamMember = TeamMember.builder()
                .id(teamMemberId)
                .role(newTeamMemberDto.role())
                .build();
        teamMember = teamMemberRepository.save(teamMember);
        log.info("Member id = '{}' was added to team event id = '{}' by user id = '{}'",
                teamMember.getId().getUserId(), teamMember.getId().getEventId(), userId);
        return teamMemberMapper.toTeamMemberDto(teamMember);
    }

    @Override
    public List<TeamMemberDto> getTeamsByEventId(Long userId, Long eventId) {
        Event event = eventService.getEventByEventId(eventId, userId);
        List<TeamMember> teamMembers = teamMemberRepository.findAllByIdEventId(eventId);
        log.debug("Found '{}' team members by event id = '{}' by user id ='{}'", teamMembers.size(), eventId, userId);
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
                () -> new NotFoundException(String.format("Team member id = %d is not in team event id = %d", memberId, eventId))
        );
    }

    private void checkTeamMemberManagerOrOwnerRoleInEvent(Long eventId, Long memberId) {
        Event event = eventService.getEventByEventId(eventId, memberId);
        if (!event.getOwnerId().equals(memberId)) {
            TeamMember user = getTeamMember(eventId, memberId);
            if (!user.getRole().equals(TeamMemberRole.MANAGER)) {
                throw new NotAuthorizedException(String.format("User id = %d in event id = %d not Manager", memberId, eventId));
            }
        }
    }

    private void checkUserExists(Long userId, Long memberId) {
        UserDto userDto = userClient.getUserById(userId, memberId);
    }

}