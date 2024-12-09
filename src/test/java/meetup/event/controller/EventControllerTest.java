package meetup.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import meetup.event.dto.event.EventDto;
import meetup.event.dto.event.NewEventDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.event.Event;
import meetup.event.model.teammember.TeamMemberRole;
import meetup.event.service.event.EventService;
import meetup.event.service.teammember.TeamMemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static meetup.event.model.event.RegistrationStatus.CLOSED;
import static meetup.event.model.event.RegistrationStatus.OPEN;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
class EventControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private EventService eventService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TeamMemberService teamMemberService;

    @Autowired
    private EventController eventController;


    private static final String HEADER_X_USER_ID = "X-User-Id";

    private final NewEventDto newEventDto = NewEventDto.builder()
            .name("event")
            .description("description")
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .registrationStatus(OPEN)
            .build();

    private final UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
            .name(null)
            .description("description upd")
            .startDateTime(null)
            .endDateTime(LocalDateTime.of(2024, 12, 28, 22, 0, 0))
            .location(null)
            .registrationStatus(CLOSED)
            .build();

    private final Event event = Event.builder()
            .id(1L).name("event")
            .description("event description")
            .createdDateTime(null)
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .ownerId(null)
            .registrationStatus(OPEN)
            .build();

    private final EventDto eventDto = EventDto.builder()
            .id(1L)
            .name("event")
            .description("event description")
            .createdDateTime(LocalDateTime.now())
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .ownerId(null)
            .registrationStatus(OPEN)
            .build();

    @Test
    void createEvent() throws Exception {
        when(eventService.createEvent(anyLong(), any()))
                .thenReturn(event);
        when(eventService.createEvent(1L, event))
                .thenReturn(event);
        when(eventMapper.toEventDto(event))
                .thenReturn(eventDto);

        mvc.perform(post("/events")
                        .content(mapper.writeValueAsString(newEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26 18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26 22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.registrationStatus", is("OPEN")));

        verify(eventMapper, times(1)).toEventFromNewEventDto(newEventDto);
        verify(eventService, times(1)).createEvent(anyLong(), any());
        verify(eventMapper, times(1)).toEventDto(event);
    }

    @Test
    void createEventWithBlankName() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithBlankDescription() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 00, 00))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 00, 00))
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithPastStartDateTime() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(LocalDateTime.of(2022, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithNullStartDateTime() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(null)
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithPastEndDateTime() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2023, 12, 26, 22, 0, 0))
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithNullEndDateTime() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(null)
                .location("location")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithBlankLocation() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("")
                .registrationStatus(OPEN)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void createEventWithNullRegistrationStatus() throws Exception {
        NewEventDto newEventDto = NewEventDto.builder()
                .name("name")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .registrationStatus(null)
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventMapper, never()).toEventDto(any());
        verify(eventService, never()).createEvent(anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void updateEvent() throws Exception {
        when(eventService.updateEvent(anyLong(), anyLong(), any()))
                .thenReturn(event);
        when(eventService.updateEvent(1L, eventDto.id(), updatedEventDto))
                .thenReturn(event);
        when(eventMapper.toEventDto(event))
                .thenReturn(eventDto);

        mvc.perform(patch("/events/1")
                        .content(mapper.writeValueAsString(updatedEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26 18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26 22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.registrationStatus", is("OPEN")));

        verify(eventService, times(1)).updateEvent(anyLong(), anyLong(), any());
        verify(eventMapper, times(1)).toEventDto(event);
    }

    @Test
    void updateEventWithBlankName() throws Exception {
        UpdatedEventDto updatedEvent = UpdatedEventDto.builder()
                .name("")
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("location")
                .build();

        mvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedEvent))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventService, never()).updateEvent(anyLong(), anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void updateEventWithBlankLocation() throws Exception {
        UpdatedEventDto updatedEvent = UpdatedEventDto.builder()
                .name(null)
                .description("description")
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
                .location("")
                .build();

        mvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedEvent))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));

        verify(eventService, never()).updateEvent(anyLong(), anyLong(), any());
        verify(eventMapper, never()).toEventDto(any());
    }

    @Test
    void getEventById() throws Exception {
        when(eventService.getEventByEventId(anyLong(), anyLong()))
                .thenReturn(event);
        when(eventService.getEventByEventId(1L, 1L))
                .thenReturn(event);
        when(eventMapper.toEventDto(event))
                .thenReturn(eventDto);

        mvc.perform(get("/events/1")
                        .content(mapper.writeValueAsString(newEventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26 18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26 22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.registrationStatus", is("OPEN")));

        verify(eventService, times(1)).getEventByEventId(anyLong(), anyLong());
        verify(eventMapper, times(1)).toEventDto(event);
    }

    @Test
    void getEvents() throws Exception {
        List<Event> events = new ArrayList<>();

        when(eventService.getEvents(anyInt(), anyInt(), any()))
                .thenReturn(events);

        mvc.perform(get("/events?from=0&size=20&userId=1")
                        .content(mapper.writeValueAsString(events))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("from", "0")
                        .queryParam("size", "20")
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEvents(anyInt(), anyInt(), any());
        verify(eventMapper, times(1)).toDtoList(events);
    }

    @Test
    void getEventsWithNegativeFrom() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=-3&size=20&userId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), any());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void getEventsWithNegativeSize() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=3&size=-20&userId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), any());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void getEventsWithZeroSize() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=3&size=0&userId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), any());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void deleteEventById() throws Exception {
        mvc.perform(delete("/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_X_USER_ID, 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void addTeamMember_shouldReturnCreatedTeamMember() {
        Long userId = 1L;
        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(10L, 2L, TeamMemberRole.MEMBER);
        TeamMemberDto teamMemberDto = new TeamMemberDto(10L, 2L, TeamMemberRole.MEMBER);

        when(teamMemberService.addTeamMember(userId, newTeamMemberDto)).thenReturn(teamMemberDto);

        ResponseEntity<TeamMemberDto> response = eventController.addTeamMember(userId, newTeamMemberDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(teamMemberDto, response.getBody());
        verify(teamMemberService, times(1)).addTeamMember(userId, newTeamMemberDto);
    }

    @Test
    void addTeamMember_shouldFailValidationWhenNewTeamMemberDtoIsInvalid() throws Exception {
        Long userId = 1L;

        NewTeamMemberDto invalidDto = new NewTeamMemberDto(null, 2L, null);

        mvc.perform(post("/events/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto))
                        .header(HEADER_X_USER_ID, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

    @Test
    void getTeamsByEventId_shouldReturnListOfTeamMembers() {
        Long userId = 1L;
        Long eventId = 10L;
        List<TeamMemberDto> teamMembers = List.of(new TeamMemberDto(10L, 2L, TeamMemberRole.MEMBER));

        when(teamMemberService.getTeamsByEventId(userId, eventId)).thenReturn(teamMembers);

        ResponseEntity<List<TeamMemberDto>> response = eventController.getTeamsByEventId(userId, eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(teamMembers, response.getBody());
        verify(teamMemberService, times(1)).getTeamsByEventId(userId, eventId);
    }

    @Test
    void getTeamsByEventId_shouldReturnEmptyListIfNoTeamMembersFound() {
        Long userId = 1L;
        Long eventId = 10L;

        when(teamMemberService.getTeamsByEventId(userId, eventId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamMemberDto>> response = eventController.getTeamsByEventId(userId, eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(teamMemberService, times(1)).getTeamsByEventId(userId, eventId);
    }

    @Test
    void updateTeamMemberInEvent_shouldUpdateAndReturnUpdatedTeamMember() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;
        UpdateTeamMemberDto updateTeamMemberDto = new UpdateTeamMemberDto(TeamMemberRole.MANAGER);
        TeamMemberDto updatedDto = new TeamMemberDto(10L, 2L, TeamMemberRole.MANAGER);

        when(teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto)).thenReturn(updatedDto);

        ResponseEntity<TeamMemberDto> response = eventController.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());
        verify(teamMemberService, times(1)).updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);
    }

    @Test
    void updateTeamMemberInEvent_shouldThrowExceptionWhenDtoIsInvalid() throws Exception {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        UpdateTeamMemberDto invalidDto = new UpdateTeamMemberDto(null);

        mvc.perform(patch("/events/teams/{eventId}/{memberId}", eventId, memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto))
                        .header(HEADER_X_USER_ID, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

    @Test
    void deleteTeamMemberFromEvent_shouldDeleteTeamMemberSuccessfully() {
        Long userId = 1L;
        Long eventId = 10L;
        Long memberId = 2L;

        doNothing().when(teamMemberService).deleteTeamMemberFromEvent(userId, eventId, memberId);

        ResponseEntity<Void> response = eventController.deleteTeamMemberFromEvent(userId, eventId, memberId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(teamMemberService, times(1)).deleteTeamMemberFromEvent(userId, eventId, memberId);
    }

}