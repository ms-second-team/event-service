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
import meetup.event.service.EventService;
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
    private final EventMapper mapper;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @RequestBody @Valid NewEventDto newEventDto) {

        log.info("---START CREATE EVENT ENDPOINT---");

        EventDto eventDto = mapper.toEventDto(service.createEvent(userId, newEventDto));

        return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @PathVariable Long id,
                                                @RequestBody @Valid UpdatedEventDto
                                                        updatedEventDto) {

        log.info("---START UPDATE EVENT ENDPOINT---");

        return new ResponseEntity<>(mapper.toEventDto(service.updateEvent(userId, id,
                updatedEventDto)), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@RequestHeader(SHARER_USER_ID) Long userId,
                                                 @PathVariable Long id) {

        log.info("---START GET EVENT BY ID ENDPOINT---");

        return new ResponseEntity<>(mapper.toEventDto(service.getEventById(id, userId)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("---START GET EVENTS ENDPOINT---");

        return new ResponseEntity<>(mapper.toDtoList(service.getEvents(from, size, userId)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@RequestHeader(SHARER_USER_ID) Long userId,
                                                @PathVariable Long id) {

        log.info("---START DELETE EVENT BY ID ENDPOINT--");

        service.deleteEventById(userId, id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}