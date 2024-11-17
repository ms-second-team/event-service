package meetup.event.service;

import lombok.RequiredArgsConstructor;
import meetup.event.dto.NewEventDto;
import meetup.event.dto.UpdatedEventDto;
import meetup.event.model.Event;
import meetup.exception.NotFoundException;
import meetup.location.Location;
import meetup.location.LocationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventServiceImplTest {
    private final EventService eventService;
    private final LocationDto locationDto = new LocationDto("Локация №1", 55.7855F, 37.7610F);

    private final NewEventDto newEventDto = new NewEventDto(
            "Событие №1", "Описание события №1",
            LocalDateTime.of(2024, 12, 23, 18, 00, 00),
            LocalDateTime.of(2024, 12, 23, 22, 00, 00),
            locationDto);

    private final Location location = new Location(1L, "Локация №1", 55.7855F, 37.7610F);

    private final Event event = new Event(
            1L, "Событие №1", "Описание события №1",
            LocalDateTime.now(),
            LocalDateTime.of(2024, 12, 23, 18, 00, 00),
            LocalDateTime.of(2024, 12, 23, 22, 00, 00),
            location, 1L);

    private final UpdatedEventDto updatedEventDto = new UpdatedEventDto(
            null, "Обновленное описание события №1", null,
            LocalDateTime.of(2024, 12, 23, 23, 00, 00),
            null);

    @Test
    void createEvent() {
        Event event = eventService.createEvent(1L, newEventDto);

        Event savedEvent = eventService.getEventById(event.getId(), 1L);

        assertEquals(savedEvent, event);
    }

    @Test
    void updateEvent() {
        Event event = eventService.createEvent(1L, newEventDto);

        Event updatedEvent = eventService.updateEvent(1L, event.getId(), updatedEventDto);

        assertEquals(updatedEvent.getDescription(), updatedEventDto.description());
        assertEquals(updatedEvent.getEndDateTime(), updatedEventDto.endDateTime());
    }

    @Test
    void getEventById() {
        Event event = eventService.createEvent(1L, newEventDto);

        Event savedEvent = eventService.getEventById(event.getId(), 1L);

        assertEquals(savedEvent, event);
    }

    @Test
    void getEvents() {
        Event event = eventService.createEvent(1L, newEventDto);

        List<Event> actualEvents = eventService.getEvents(0, 10, null);

        assertFalse(actualEvents.isEmpty());
    }

    @Test
    void deleteEventById() {
        Event event = eventService.createEvent(1L, newEventDto);

        eventService.deleteEventById(1L, event.getId());

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> eventService.getEventById(event.getId(), 1L),
                "Событие id=" + event.getId() + " не найдено"
        );

        assertEquals("Событие id=" + event.getId() + " не найдено", thrown.getMessage());
    }
}