package meetup.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import meetup.event.dto.UserDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.event.Event;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "app.user-service.url=localhost:${wiremock.server.port}"
})
class EventServiceImplTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine");

    @Mock
    private EventMapper eventMapper;

    @Autowired
    private EventService eventService;
    private ObjectMapper objectMapper;
    private final Long userId = 1L;
    private final Event event = Event.builder()
            .id(null)
            .name("event")
            .description("event description")
            .createdDateTime(null)
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .ownerId(null)
            .build();

    private final UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
            .name(null)
            .description("updatedEvent description")
            .startDateTime(LocalDateTime.of(2024, 12, 27, 19, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 27, 23, 0, 0))
            .location("updated location")
            .participantLimit(10)
            .build();

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Test
    void createEvent() throws JsonProcessingException {
        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);


        Event receivedEvent = eventService.getEventByEventId(savedEvent.getId(), userId);

        assertEquals(receivedEvent.getLocation(), savedEvent.getLocation());
        assertEquals(receivedEvent.getOwnerId(), userId);
        assertEquals(0, receivedEvent.getParticipantLimit());
    }

    @Test
    void createEventWithStartDateTimeIsAfterEndDateTime() {
        Event event = Event.builder()
                .id(null)
                .name("event")
                .description("event description")
                .createdDateTime(null)
                .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
                .endDateTime(LocalDateTime.of(2023, 12, 26, 22, 0, 0))
                .location("location")
                .ownerId(null)
                .build();

        DateTimeException thrown = assertThrows(
                DateTimeException.class,
                () -> eventService.createEvent(userId, event),
                ""
        );

        assertEquals("End dateTime: " + event.getEndDateTime() + " is befofe start dateTime: "
                + event.getStartDateTime(), thrown.getMessage());
    }

    @Test
    void createEventWithNotExistUser() {

        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        NotFoundException ex = Assert.assertThrows(NotFoundException.class,
                () -> eventService.createEvent(userId, event));

        assertEquals("User was not found", ex.getLocalizedMessage());
    }

    @Test
    void updateEvent() throws JsonProcessingException {
        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        Event updatedEvent = eventService.updateEvent(userId, savedEvent.getId(), updatedEventDto);

        assertEquals(savedEvent.getId(), updatedEvent.getId());
        assertEquals(savedEvent.getName(), updatedEvent.getName());
        assertEquals(updatedEventDto.description(), updatedEvent.getDescription());
        assertEquals(savedEvent.getCreatedDateTime(), updatedEvent.getCreatedDateTime());
        assertEquals(updatedEventDto.startDateTime(), updatedEvent.getStartDateTime());
        assertEquals(updatedEventDto.endDateTime(), updatedEvent.getEndDateTime());
        assertEquals(updatedEvent.getLocation(), updatedEventDto.location());
        assertEquals(savedEvent.getOwnerId(), updatedEvent.getOwnerId());
        assertEquals(updatedEventDto.participantLimit(), updatedEvent.getParticipantLimit());
    }

    @Test
    void updateNonExistEvent() throws JsonProcessingException {
        Long eventId = 777L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.updateEvent(userId, eventId, updatedEventDto),
                ""
        );

        assertEquals("Event with id=777 was not found", thrown.getMessage());
    }

    @Test
    void updateEventByAnotherUser() throws JsonProcessingException {
        Long otherUserId = 777L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        NotAuthorizedException thrown = assertThrows(
                NotAuthorizedException.class,
                () -> eventService.updateEvent(otherUserId, savedEvent.getId(), updatedEventDto),
                ""
        );

        assertEquals("User id=777 is not the owner of the event id=" + savedEvent.getId(), thrown.getMessage());
    }

    @Test
    void updateEventWithStartDateTimeIsAfterEndDateTime() {
        Event savedEvent = eventService.createEvent(userId, event);

        UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
                .name(null)
                .description(null)
                .startDateTime(LocalDateTime.of(2024, 12, 27, 19, 0, 0))
                .endDateTime(LocalDateTime.of(2023, 12, 27, 23, 0, 0))
                .location(null)
                .build();

        DateTimeException thrown = assertThrows(
                DateTimeException.class,
                () -> eventService.updateEvent(userId, savedEvent.getId(), updatedEventDto),
                ""
        );

        assertEquals("End dateTime: " + updatedEventDto.endDateTime() + " is befofe start dateTime: "
                + updatedEventDto.startDateTime(), thrown.getMessage());
    }

    @Test
    void getEventByEventIdByOwner() throws JsonProcessingException {
        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        Event receivedEvent = eventService.getEventByEventId(savedEvent.getId(), userId);

        assertEquals(savedEvent.getId(), receivedEvent.getId());
        assertNotEquals(savedEvent.getCreatedDateTime(), null);
    }

    @Test
    void getEventByEventIdByOtherUser() throws JsonProcessingException {
        Long otherUserId = 777L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        Event receivedEvent = eventService.getEventByEventId(savedEvent.getId(), otherUserId);

        assertEquals(savedEvent.getId(), receivedEvent.getId());
        assertNull(receivedEvent.getCreatedDateTime());
    }

    @Test
    void getNonExistEvent() {
        Long eventId = 777L;

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.getEventByEventId(eventId, userId),
                ""
        );

        assertEquals("Event with id=777 was not found", thrown.getMessage());
    }

    @Test
    void getEventsWithUserId() {
        eventService.createEvent(userId, event);

        List<Event> eventList = eventService.getEvents(0, 10, userId);

        assertFalse(eventList.isEmpty());
    }

    @Test
    void getEventsWithoutUserId() throws JsonProcessingException {
        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        eventService.createEvent(userId, event);

        List<Event> eventList = eventService.getEvents(0, 10, null);

        assertFalse(eventList.isEmpty());
    }

    @Test
    void deleteEventById() throws JsonProcessingException {
        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        eventService.deleteEventById(userId, savedEvent.getId());

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.getEventByEventId(savedEvent.getId(), userId),
                ""
        );

        assertEquals("Event with id=" + savedEvent.getId() + " was not found", thrown.getMessage());
    }

    @Test
    void deleteNonExistEvent() {
        Long eventId = 777L;

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.deleteEventById(userId, eventId),
                ""
        );

        assertEquals("Event with id=777 was not found", thrown.getMessage());
    }

    @Test
    void deleteEventByOtherUser() throws JsonProcessingException {
        Long otherUserId = 777L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        Event savedEvent = eventService.createEvent(userId, event);

        NotAuthorizedException thrown = assertThrows(
                NotAuthorizedException.class,
                () -> eventService.deleteEventById(otherUserId, savedEvent.getId()),
                ""
        );

        assertEquals("User id=777 is not the owner of the event id=" + savedEvent.getId(), thrown.getMessage());
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