package meetup.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import meetup.event.dto.EventDto;
import meetup.event.dto.NewEventDto;
import meetup.event.dto.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.event.service.EventService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    private final NewEventDto newEventDto = NewEventDto.builder()
            .name("event")
            .description("description")
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .build();

    private final UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
            .name(null)
            .description("description upd")
            .startDateTime(null)
            .endDateTime(LocalDateTime.of(2024, 12, 28, 22, 0, 0))
            .location(null)
            .build();

    private final Event event = Event.builder()
            .id(1L).name("event")
            .description("event description")
            .createdDateTime(null)
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .ownerId(null)
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
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26T18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26T22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class));

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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                .build();

        mvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newEventDto))
                        .header(SHARER_USER_ID, 1))
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
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26T18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26T22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class));

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
                        .header(SHARER_USER_ID, 1))
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
                        .header(SHARER_USER_ID, 1))
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
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventDto.id()), Long.class))
                .andExpect(jsonPath("$.name", is(eventDto.name())))
                .andExpect(jsonPath("$.description", is(eventDto.description())))
                .andExpect(jsonPath("$.startDateTime", is("2024-12-26T18:00:00")))
                .andExpect(jsonPath("$.endDateTime", is("2024-12-26T22:00:00")))
                .andExpect(jsonPath("$.location", is("location")))
                .andExpect(jsonPath("$.ownerId", is(event.getOwnerId()), Long.class));

        verify(eventService, times(1)).getEventByEventId(anyLong(), anyLong());
        verify(eventMapper, times(1)).toEventDto(event);
    }

    @Test
    void getEvents() throws Exception {
        List<Event> events = new ArrayList<>();

        when(eventService.getEvents(anyInt(), anyInt(), anyLong()))
                .thenReturn(events);

        mvc.perform(get("/events?from=0&size=20&userId=1")
                        .content(mapper.writeValueAsString(events))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("from", "0")
                        .queryParam("size", "20")
                        .queryParam("userId", "1")
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEvents(anyInt(), anyInt(), anyLong());
        verify(eventMapper, times(1)).toDtoList(events);
    }

    @Test
    void getEventsWithNegativeFrom() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=-3&size=20&userId=")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), anyLong());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void getEventsWithNegativeSize() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=3&size=-20&userId=")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), anyLong());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void getEventsWithZeroSize() throws Exception {
        List<Event> events = new ArrayList<>();

        mvc.perform(get("/events?from=3&size=0&userId=")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(events))
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> Assertions.assertNotNull(result.getResolvedException()))
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(eventService, never()).getEvents(anyInt(), anyInt(), anyLong());
        verify(eventMapper, never()).toDtoList(any());
    }

    @Test
    void deleteEventById() throws Exception {
        mvc.perform(delete("/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isNoContent());
    }

}