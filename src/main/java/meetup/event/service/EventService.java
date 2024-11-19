package meetup.event.service;

import meetup.event.model.Event;
import meetup.event.dto.UpdatedEventDto;

import java.util.List;

public interface EventService {
    Event createEvent(Long userId, Event event);

    Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto);

    Event getEventByEventId(Long eventId, Long userId);

    List<Event> getEvents(Integer from, Integer size, Long userId);

    void deleteEventById(Long userId, Long eventId);
}