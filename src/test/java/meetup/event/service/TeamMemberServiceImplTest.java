package meetup.event.service;

import meetup.event.dto.NewTeamMemberDto;
import meetup.event.dto.TeamMemberDto;
import meetup.event.dto.UpdateTeamMemberDto;
import meetup.event.mapper.TeamMemberMapper;
import meetup.event.model.Event;
import meetup.event.model.TeamMember;
import meetup.event.model.TeamMemberId;
import meetup.event.model.TeamMemberRole;
import meetup.event.repository.EventRepository;
import meetup.event.repository.TeamMemberRepository;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TeamMemberServiceImplTest {
    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addTeamMember_shouldAddTeamMemberSuccessfully() {
        Long userId = 1L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(10L, 2L, TeamMemberRole.MEMBER);
        Event event = new Event();
        event.setId(10L);
        event.setOwnerId(1L);

        TeamMember teamMember = new TeamMember(new TeamMemberId(10L, 2L), TeamMemberRole.MEMBER);
        TeamMemberDto expectedDto = new TeamMemberDto(10L, 2L, TeamMemberRole.MEMBER);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(teamMemberMapper.toTeamMember(newTeamMemberDto)).thenReturn(teamMember);
        when(teamMemberRepository.save(teamMember)).thenReturn(teamMember);
        when(teamMemberMapper.toTeamMemberDto(teamMember)).thenReturn(expectedDto);

        TeamMemberDto result = teamMemberService.addTeamMember(userId, newTeamMemberDto);

        assertEquals(expectedDto, result);
        verify(eventRepository, times(1)).findById(10L);
        verify(teamMemberRepository, times(1)).save(teamMember);
        verify(teamMemberMapper, times(1)).toTeamMemberDto(teamMember);
    }

    @Test
    void addTeamMember_shouldThrowExceptionWhenEventNotFound() {
        Long userId = 1L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(10L, 2L, TeamMemberRole.MEMBER);

        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("Event id = 10 not found!", exception.getMessage());
        verify(eventRepository, times(1)).findById(10L);
        verifyNoInteractions(teamMemberRepository, teamMemberMapper);
    }

    @Test
    void getTeamsByEventId_shouldReturnTeamMembers() {
        Long userId = 1L;
        Long eventId = 10L;

        List<TeamMember> teamMembers = List.of(new TeamMember(new TeamMemberId(10L, 2L), TeamMemberRole.MEMBER));
        List<TeamMemberDto> expectedDtos = List.of(new TeamMemberDto(10L, 2L, TeamMemberRole.MEMBER));

        when(teamMemberRepository.findAllByIdEventId(eventId)).thenReturn(teamMembers);
        when(teamMemberMapper.toTeamMemberDtoList(teamMembers)).thenReturn(expectedDtos);

        List<TeamMemberDto> result = teamMemberService.getTeamsByEventId(userId, eventId);

        assertEquals(expectedDtos, result);
        verify(teamMemberRepository, times(1)).findAllByIdEventId(eventId);
        verify(teamMemberMapper, times(1)).toTeamMemberDtoList(teamMembers);
    }

    @Test
    void getTeamsByEventId_shouldReturnEmptyListIfNoTeamMembersFound() {
        Long userId = 1L;
        Long eventId = 10L;

        when(teamMemberRepository.findAllByIdEventId(eventId)).thenReturn(List.of());

        List<TeamMemberDto> result = teamMemberService.getTeamsByEventId(userId, eventId);

        assertTrue(result.isEmpty());
        verify(teamMemberRepository, times(1)).findAllByIdEventId(eventId);
        verifyNoInteractions(teamMemberMapper);
    }


    @Test
    void updateTeamMemberInEvent_shouldUpdateSuccessfully() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        TeamMember teamMember = new TeamMember(
                new TeamMemberId(eventId, memberId),
                TeamMemberRole.MEMBER
        );
        TeamMemberDto teamMemberDto = new TeamMemberDto(
                eventId,
                memberId,
                TeamMemberRole.MANAGER
        );

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER)));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId))
                .thenReturn(Optional.of(teamMember));
        doNothing().when(teamMemberMapper).updateTeamMember(updateTeamMemberDto, teamMember);
        when(teamMemberRepository.save(teamMember)).thenReturn(teamMember);
        when(teamMemberMapper.toTeamMemberDto(teamMember)).thenReturn(teamMemberDto);

        TeamMemberDto result = teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);

        assertEquals(teamMemberDto, result);
        verify(eventRepository, times(1)).existsById(eventId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, userId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, memberId);
        verify(teamMemberRepository, times(1)).save(teamMember);
        verify(teamMemberMapper, times(1)).toTeamMemberDto(teamMember);

    }

    @Test
    void updateTeamMemberInEvent_shouldThrowNotFoundExceptionWhenEventDoesNotExist() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(eventRepository.existsById(eventId)).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("Event id = %d not found!", eventId), exception.getMessage());
        verify(eventRepository, times(1)).existsById(eventId);
        verifyNoMoreInteractions(teamMemberRepository, teamMemberMapper);
    }

    @Test
    void updateTeamMemberInEvent_shouldThrowNotFoundExceptionWhenMemberDoesNotExist() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER)));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("Team member id = %d in team event id = %d", memberId, eventId), exception.getMessage());
        verify(eventRepository, times(1)).existsById(eventId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, userId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, memberId);
        verifyNoMoreInteractions(teamMemberMapper);
    }

    @Test
    void updateTeamMemberInEvent_shouldThrowNotAuthorizedExceptionWhenUserIsNotManager() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MEMBER)));

        NotAuthorizedException exception = assertThrows(
                NotAuthorizedException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("User id = %d in event id = %d not Manager", userId, eventId), exception.getMessage());
        verify(eventRepository, times(1)).existsById(eventId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, userId);
        verifyNoMoreInteractions(teamMemberMapper);
    }

    @Test
    void deleteTeamMemberFromEvent_shouldDeleteSuccessfully() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        TeamMember teamMember = new TeamMember(new TeamMemberId(eventId, memberId), TeamMemberRole.MEMBER);
        TeamMember teamManager = new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId)).thenReturn(Optional.of(teamMember));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId)).thenReturn(Optional.of(teamManager));

        doNothing().when(teamMemberRepository).delete(teamMember);

        teamMemberService.deleteTeamMemberFromEvent(userId, eventId, memberId);

        verify(teamMemberRepository, times(1)).delete(teamMember);
    }

    @Test
    void deleteTeamMemberFromEvent_shouldThrowExceptionWhenTeamMemberNotFound() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.deleteTeamMemberFromEvent(userId, eventId, memberId));

        assertEquals("Team member id = 1 in team event id = 10", exception.getMessage());
    }
}