package meetup.event.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.repository.event.EventSpecification;
import meetup.event.client.UserClient;
import meetup.event.dto.event.EventSearchFilter;
import meetup.event.dto.user.UserDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.event.Event;
import meetup.event.model.event.RegistrationStatus;
import meetup.event.repository.event.EventRepository;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        checkUserExists(userId, userId);
        checkStartAndEndDateTime(event.getStartDateTime(), event.getEndDateTime());
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
    public List<Event> getEvents(Integer from, Integer size, EventSearchFilter filter) {
        final Pageable pageable = PageRequest.of(from, size);
        final List<Specification<Event>> specifications = searchFilterToSpecificationList(filter);
        final Specification<Event> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        final List<Event> events = eventRepository.findAll(resultSpec, pageable).getContent();

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

    private void checkUserExists(Long userId, Long ownerId) {
        UserDto userDto = userClient.getUserById(userId, ownerId);
    }

    public static RegistrationStatus findByRegistrationStatus(String name) {
        if (name == null) {
            return null;
        }
        for (RegistrationStatus status : RegistrationStatus.values()) {
            if (name.equalsIgnoreCase(status.name())) {
                return status;
            }
        }
        throw new NotFoundException("Unknown registration status: " + name);
    }


    private List<Specification<Event>> searchFilterToSpecificationList(EventSearchFilter searchFilter) {
        List<Specification<Event>> resultList = new ArrayList<>();
        resultList.add(EventSpecification.ownerIdEquals(searchFilter.userId()));
        resultList.add(EventSpecification.registrationStatusEquals(findByRegistrationStatus(searchFilter.registrationStatus())));
        return resultList.stream().filter(Objects::nonNull).toList();
    }

}