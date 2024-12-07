package meetup.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.client.UserClient;
import meetup.event.dto.UserDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.event.Event;
import meetup.event.repository.EventRepository;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public Event createEvent(Long userId, Event event) {
        checkStartAndEndDateTime(event.getStartDateTime(), event.getEndDateTime());
        UserDto userDto = userClient.getUserById(userId, userId).getBody();
        event.setOwnerId(userId);

        Event eventSaved = eventRepository.save(event);

        log.info("User with id=" + userId + " added a new event with id=" + event.getId());

        return eventSaved;
    }

    @Override
    @Transactional
    public Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto) {
        Event event = getEventById(eventId);

        checkIfTheUserIsTheOwner(userId, event);
        eventMapper.updateEvent(updatedEventDto, event);
        checkStartAndEndDateTime(event.getStartDateTime(), event.getEndDateTime());

        Event updatedEvent = eventRepository.save(event);

        log.info("User with id=" + userId + " updated event with id=" + eventId);

        return updatedEvent;
    }

    @Override
    public Event getEventByEventId(Long eventId, Long userId) {
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

    private void checkStartAndEndDateTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new DateTimeException("End dateTime: " + end + " is befofe start dateTime: " + start);
        }
    }

}