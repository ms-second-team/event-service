package meetup.event.service;

import meetup.event.model.Event;
import meetup.event.dto.UpdatedEventDto;
import meetup.location.Location;

import java.util.List;

public interface EventService {
    Event createEvent(Long userId, Event event, Location location);

    Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto, Location location);

    Event getEventById(Long eventId, Long userId);

    List<Event> getEvents(Integer from, Integer size, Long userId);

    void deleteEventById(Long userId, Long eventId);
}