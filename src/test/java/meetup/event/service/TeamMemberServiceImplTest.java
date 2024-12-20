package meetup.event.service;

import meetup.event.client.UserClient;
import meetup.event.dto.user.UserDto;
import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;
import meetup.event.mapper.TeamMemberMapper;
import meetup.event.model.event.Event;
import meetup.event.model.teammember.TeamMember;
import meetup.event.model.teammember.TeamMemberId;
import meetup.event.model.teammember.TeamMemberRole;
import meetup.event.repository.event.EventRepository;
import meetup.event.repository.teammember.TeamMemberRepository;
import meetup.event.service.event.EventService;
import meetup.event.service.teammember.TeamMemberServiceImpl;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamMemberServiceImplTest {
    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private EventRepository repository;

    @Mock
    private UserClient userClient;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addTeamMember_shouldAddTeamMemberSuccessfully() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(eventId, memberId, TeamMemberRole.MEMBER);

        Event event = new Event();
        event.setId(eventId);
        event.setOwnerId(userId);

        TeamMember teamMember = new TeamMember(new TeamMemberId(eventId, memberId), TeamMemberRole.MEMBER);
        TeamMemberDto expectedDto = new TeamMemberDto(eventId, memberId, TeamMemberRole.MEMBER);

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);
        when(teamMemberMapper.toTeamMember(newTeamMemberDto)).thenReturn(teamMember);
        UserDto userDto = createUser(memberId);
        when(userClient.getUserById(anyLong(), anyLong()))
                .thenReturn(userDto);

        when(teamMemberRepository.save(any())).thenReturn(teamMember);
        when(teamMemberMapper.toTeamMemberDto(teamMember)).thenReturn(expectedDto);

        TeamMemberDto result = teamMemberService.addTeamMember(userId, newTeamMemberDto);
        assertEquals(expectedDto, result);
        verify(teamMemberMapper, times(1)).toTeamMemberDto(teamMember);
    }

    @Test
    void addTeamMember_shouldThrowExceptionWhenEventNotFound() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(eventId, memberId, TeamMemberRole.MEMBER);

        UserDto userDto = createUser(userId);
        when(userClient.getUserById(userId, userId))
                .thenReturn(userDto);

        when(eventService.getEventByEventId(eventId, userId))
                .thenThrow(new NotFoundException("Event id = 10 not found!"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("Event id = 10 not found!", exception.getMessage());
        verifyNoInteractions(teamMemberRepository, teamMemberMapper);
    }

    @Test
    void addTeamMember_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 10L;
        Long eventId = 10L;
        Long memberId = 12L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(eventId, memberId, TeamMemberRole.MEMBER);

        when(userClient.getUserById(userId, userId))
                .thenThrow(new NotFoundException("User was not found!"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("User was not found!", exception.getMessage());
        verifyNoInteractions(teamMemberRepository, teamMemberMapper);
    }

    @Test
    void addTeamMember_shouldThrowExceptionWhenMemberNotFound() {
        Long userId = 10L;
        Long eventId = 10L;
        Long memberId = 12L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(eventId, memberId, TeamMemberRole.MEMBER);

        when(userClient.getUserById(userId, memberId))
                .thenThrow(new NotFoundException("User was not found!"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("User was not found!", exception.getMessage());
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
        Event event = Event.builder()
                .id(eventId)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(memberId)
                .build();

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);
        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER)));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId))
                .thenReturn(Optional.of(teamMember));
        doNothing().when(teamMemberMapper).updateTeamMember(updateTeamMemberDto, teamMember);
        when(teamMemberRepository.save(teamMember)).thenReturn(teamMember);
        when(teamMemberMapper.toTeamMemberDto(teamMember)).thenReturn(teamMemberDto);

        TeamMemberDto result = teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);

        assertEquals(teamMemberDto, result);
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
        when(eventService.getEventByEventId(eventId, userId))
                .thenThrow(new NotFoundException(String.format("Event id = %d not found!", eventId)));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("Event id = %d not found!", eventId), exception.getMessage());
        verifyNoMoreInteractions(teamMemberRepository, teamMemberMapper);
    }

    @Test
    void updateTeamMemberInEvent_shouldThrowNotFoundExceptionWhenMemberDoesNotExist() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(memberId)
                .build();

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER)));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("Team member id = %d is not in team event id = %d", memberId, eventId), exception.getMessage());
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, userId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, memberId);
        verifyNoMoreInteractions(teamMemberMapper);
    }

    @Test
    void updateTeamMemberInEvent_shouldThrowNotAuthorizedExceptionWhenUserIsNotManager() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(memberId)
                .build();

        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MEMBER)));
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, userId))
                .thenReturn(Optional.of(new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MEMBER)));


        NotAuthorizedException exception = assertThrows(
                NotAuthorizedException.class,
                () -> teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)
        );

        assertEquals(String.format("User id = %d in event id = %d not Manager", userId, eventId), exception.getMessage());
        verify(eventService, times(1)).getEventByEventId(eventId, userId);
        verify(teamMemberRepository, times(1)).findByIdEventIdAndIdUserId(eventId, userId);
        verifyNoMoreInteractions(teamMemberMapper);
    }

    @Test
    void deleteTeamMemberFromEvent_shouldDeleteSuccessfully() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(memberId)
                .build();
        TeamMember teamMember = new TeamMember(new TeamMemberId(eventId, memberId), TeamMemberRole.MEMBER);
        TeamMember teamManager = new TeamMember(new TeamMemberId(eventId, userId), TeamMemberRole.MANAGER);

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);
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
        Event event = Event.builder()
                .id(eventId)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(memberId)
                .build();

        when(eventService.getEventByEventId(eventId, userId)).thenReturn(event);
        when(teamMemberRepository.findByIdEventIdAndIdUserId(eventId, memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> teamMemberService.deleteTeamMemberFromEvent(userId, eventId, memberId));

        assertEquals("Team member id = 1 is not in team event id = 10", exception.getMessage());
    }

    private UserDto createUser(long userId) {
        return new UserDto(
                userId,
                "John",
                "john@example.com",
                "StrongP@ss1",
                "Hello");
    }

}