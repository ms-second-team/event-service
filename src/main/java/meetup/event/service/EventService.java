package meetup.event.service;

import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.model.event.Event;

import java.util.List;

public interface EventService {
    Event createEvent(Long userId, Event event);

    Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto);

    Event getEventByEventId(Long eventId, Long userId);

    List<Event> getEvents(Integer from, Integer size, Long userId);

    void deleteEventById(Long userId, Long eventId);
}