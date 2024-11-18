package meetup.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.mapper.EventMapper;
import meetup.event.dto.EventDto;
import meetup.event.dto.NewEventDto;
import meetup.event.dto.UpdatedEventDto;
import meetup.event.model.Event;
import meetup.event.service.EventService;
import meetup.location.Location;
import meetup.location.LocationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventController {
    private final EventService service;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @RequestBody @Valid NewEventDto newEventDto) {

        log.info("---START CREATE EVENT ENDPOINT---");

        Event event = eventMapper.toEventFromNewEventDto(newEventDto);
        Location location = locationMapper.toLocation(newEventDto.location());
        Event eventCreated = service.createEvent(userId, event, location);
        EventDto eventDto = eventMapper.toEventDto(eventCreated);

        return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @PathVariable Long id,
                                                @RequestBody @Valid UpdatedEventDto
                                                        updatedEventDto) {

        log.info("---START UPDATE EVENT ENDPOINT---");

        Location location = locationMapper.toLocation(updatedEventDto.location());
        Event event = service.updateEvent(userId, id, updatedEventDto, location);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@RequestHeader(SHARER_USER_ID) Long userId,
                                                 @PathVariable Long id) {

        log.info("---START GET EVENT BY ID ENDPOINT---");

        Event event = service.getEventById(id, userId);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("---START GET EVENTS ENDPOINT---");

        List<Event> events = service.getEvents(from, size, userId);
        List<EventDto> eventsDto = eventMapper.toDtoList(events);

        return new ResponseEntity<>(eventsDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @PathVariable Long id) {

        log.info("---START DELETE EVENT BY ID ENDPOINT--");

        service.deleteEventById(userId, id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}