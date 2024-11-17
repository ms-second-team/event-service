package meetup.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.event.repository.EventRepository;
import meetup.event.dto.NewEventDto;
import meetup.event.dto.UpdatedEventDto;
import meetup.exception.NotFoundException;
import meetup.location.Location;
import meetup.location.LocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventMapper mapper;

    @Override
    public Event createEvent(Long userId, NewEventDto newEventDto) {
        Location location = locationRepository.save(mapper.toLocation(newEventDto.location()));

        Event event = mapper.toEventFromNewEventDto(newEventDto);
        event.setOwnerId(userId);
        event.setLocation(location);
        event.setCreatedDateTime(LocalDateTime.now());

        eventRepository.save(event);

        log.info("Пользователь с id=" + userId + "добавил новое событие id=" + event.getId());

        return event;
    }

    @Override
    public Event updateEvent(Long userId, Long eventId, UpdatedEventDto updatedEventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Событие id=" + eventId + " не найдено"));

        if (!Objects.equals(event.getOwnerId(), userId)) {
            throw new NotFoundException("Пользователь id=" + userId + " не является создателем события id=" + eventId);
        }

        if (updatedEventDto.name() != null) {
            event.setName(updatedEventDto.name());
        }
        if (updatedEventDto.description() != null) {
            event.setDescription(updatedEventDto.description());
        }
        if (updatedEventDto.startDateTime() != null) {
            event.setStartDateTime(updatedEventDto.startDateTime());
        }
        if (updatedEventDto.endDateTime() != null) {
            event.setEndDateTime(updatedEventDto.endDateTime());
        }
        if (updatedEventDto.location() != null) {
            Location location = locationRepository.save(mapper.toLocation(updatedEventDto.location()));
            event.setLocation(location);
        }

        log.info("Пользователь id=" + userId + " обновил событие id=" + eventId);

        return event;
    }

    @Override
    public Event getEventById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Событие id=" + eventId + " не найдено"));

        if (!Objects.equals(event.getOwnerId(), userId)) {
            event.setCreatedDateTime(null);
        }

        log.info("Найдено событие id=" + eventId);

        return event;
    }

    @Override
    public List<Event> getEvents(Integer from, Integer size, Long userId) {
        final Pageable pageable = PageRequest.of(from, size);
        List<Event> events;
        if (userId != null) {
            events = eventRepository.findAllByOwnerId(userId, pageable);
            log.info("Сформирован список событий с фильтром по пользователю id=" + userId);
        } else {
            events = eventRepository.findAll(pageable).getContent();
            log.info("Сформирован список всех событий");
        }
        return events;
    }

    @Override
    public void deleteEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Событие id=" + eventId + " не найдено"));

        if (Objects.equals(event.getOwnerId(), userId)) {
            eventRepository.deleteById(eventId);
        } else {
            throw new NotFoundException("Пользователь id=" + userId + " не является создателем события id=" + eventId
                    + ". Удаление невозможно.");
        }

        log.info("Событие id=" + eventId + " удалено");
    }

}