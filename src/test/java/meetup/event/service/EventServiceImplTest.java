package meetup.event.service;

import meetup.EventServiceApplication;
import meetup.event.dto.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = EventServiceApplication.class)
@Testcontainers
class EventServiceImplTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine");

    @MockBean
    private EventMapper eventMapper;

    @Autowired
    EventService eventService;

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
            .build();

    @Test
    void createEvent() {
        Event savedEvent = eventService.createEvent(userId, event);

        Event receivedEvent = eventService.getEventByEventId(savedEvent.getId(), userId);

        assertEquals(receivedEvent.getLocation(), savedEvent.getLocation());
        assertEquals(receivedEvent.getOwnerId(), userId);
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
    void updateEvent() {
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
    }

    @Test
    void updateNonExistEvent() {
        Long eventId = 777L;
        Event savedEvent = eventService.createEvent(userId, event);

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.updateEvent(userId, eventId, updatedEventDto),
                ""
        );

        assertEquals("Event with id=777 was not found", thrown.getMessage());
    }

    @Test
    void updateEventByAnotherUser() {
        Long otherUserId = 777L;
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
    void getEventByEventIdByOwner() {
        Event savedEvent = eventService.createEvent(userId, event);

        Event receivedEvent = eventService.getEventByEventId(savedEvent.getId(), userId);

        assertEquals(savedEvent.getId(), receivedEvent.getId());
        assertNotEquals(savedEvent.getCreatedDateTime(), null);
    }

    @Test
    void getEventByEventIdByOtherUser() {
        Long otherUserId = 777L;

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
    void getEventsWithoutUserId() {
        eventService.createEvent(userId, event);

        List<Event> eventList = eventService.getEvents(0, 10, null);

        assertFalse(eventList.isEmpty());
    }

    @Test
    void deleteEventById() {
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
    void deleteEventByOtherUser() {
        Long otherUserId = 777L;
        Event savedEvent = eventService.createEvent(userId, event);

        NotAuthorizedException thrown = assertThrows(
                NotAuthorizedException.class,
                () -> eventService.deleteEventById(otherUserId, savedEvent.getId()),
                ""
        );

        assertEquals("User id=777 is not the owner of the event id=" + savedEvent.getId(), thrown.getMessage());
    }

}