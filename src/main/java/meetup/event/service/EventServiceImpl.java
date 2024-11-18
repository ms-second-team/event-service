package meetup.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.event.repository.EventRepository;
import meetup.event.dto.UpdatedEventDto;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import meetup.location.Location;
import meetup.location.LocationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public Event createEvent(Long userId, Event event, Location location) {
        Location locationCreated = locationService.createLocation(location);

        event.setLocation(locationCreated);
        event.setOwnerId(userId);
        eventRepository.save(event);

        log.info("User with id=" + userId + " added a new event with id=" + event.getId());

        return event;
    }

    @Override
    @Transactional
    public Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto, Location location) {
        Event event = getEventById(eventId);

        locationService.createLocation(location);
        checkIfTheUserIsTheOwner(userId, event);
        eventMapper.updateEvent(updatedEventDto, event);

        Event updatedEvent = eventRepository.save(event);

        log.info("User with id=" + userId + " updated event with id=" + eventId);

        return updatedEvent;
    }

    @Override
    public Event getEventById(Long eventId, Long userId) {
        Event event = getEventById(eventId);

        if (!Objects.equals(event.getOwnerId(), userId)) {
            event.setCreatedDateTime(null);
        }

        log.info("Event with id=" + eventId + " found");

        return event;
    }

    @Override
    public List<Event> getEvents(Integer from, Integer size, Long userId) {
        final Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByOwnerId(userId, pageable);

        log.info("A list of events has been generated");

        return events;
    }

    @Override
    public void deleteEventById(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        checkIfTheUserIsTheOwner(userId, event);
        eventRepository.deleteById(eventId);

        log.info("Event with id=" + eventId + "deleted");
    }

    private Event getEventById(Long eventId) {

        return eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkIfTheUserIsTheOwner(Long userId, Event event) {
        if (!Objects.equals(event.getOwnerId(), userId)) {
            throw new NotAuthorizedException("User id=" + userId + " is not the owner of the event id=" + event.getId());
        }
    }

}