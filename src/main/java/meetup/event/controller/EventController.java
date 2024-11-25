package meetup.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetup.event.dto.event.EventDto;
import meetup.event.dto.event.NewEventDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.event.Event;
import meetup.event.service.EventService;
import meetup.event.service.TeamMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventController {
    private final EventService eventService;
    private final TeamMemberService teamMemberService;
    private final EventMapper eventMapper;
    private static final String HEADER_X_USER_ID = "X-User-Id";

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @RequestBody @Valid NewEventDto newEventDto) {

        log.info("---START CREATE EVENT ENDPOINT---");

        Event event = eventMapper.toEventFromNewEventDto(newEventDto);
        Event eventCreated = eventService.createEvent(userId, event);
        EventDto eventDto = eventMapper.toEventDto(eventCreated);

        return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @PathVariable Long id,
                                                @RequestBody @Valid UpdatedEventDto
                                                        updatedEventDto) {

        log.info("---START UPDATE EVENT ENDPOINT---");

        Event event = eventService.updateEvent(userId, id, updatedEventDto);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@RequestHeader(HEADER_X_USER_ID) Long userId,
                                                 @PathVariable Long id) {

        log.info("---START GET EVENT BY ID ENDPOINT---");

        Event event = eventService.getEventByEventId(id, userId);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("---START GET EVENTS ENDPOINT---");

        List<Event> events = eventService.getEvents(from, size, userId);
        List<EventDto> eventsDto = eventMapper.toDtoList(events);

        return new ResponseEntity<>(eventsDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @PathVariable Long id) {

        log.info("---START DELETE EVENT BY ID ENDPOINT--");

        eventService.deleteEventById(userId, id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/teams")
    public ResponseEntity<TeamMemberDto> addTeamMember(
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @RequestBody @Valid NewTeamMemberDto newTeamMemberDto) {
        log.debug("Creating team member id = '{}' event id = '{}' by user id = '{}'",
                newTeamMemberDto.userId(), newTeamMemberDto.eventId(), userId);
        TeamMemberDto teamMemberDto = teamMemberService.addTeamMember(userId, newTeamMemberDto);
        return new ResponseEntity<>(teamMemberDto, HttpStatus.CREATED);
    }

    @GetMapping("/teams/{eventId}")
    public ResponseEntity<List<TeamMemberDto>> getTeamsByEventId(
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @PathVariable Long eventId) {
        log.debug("User id = '{}' requests team info event id = '{}'", userId, eventId);
        List<TeamMemberDto> teamMemberDtos = teamMemberService.getTeamsByEventId(userId, eventId);
        return new ResponseEntity<>(teamMemberDtos, HttpStatus.OK);
    }

    @PatchMapping("/teams/{eventId}/{memberId}")
    public ResponseEntity<TeamMemberDto> updateTeamMemberInEvent(
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @PathVariable Long eventId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateTeamMemberDto updateTeamMemberDto) {
        log.debug("Updating team member id = '{}' in team event id = '{}' by user id = '{}'", memberId, eventId, userId);
        TeamMemberDto teamMemberDto = teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);
        return new ResponseEntity<>(teamMemberDto, HttpStatus.OK);
    }

    @DeleteMapping("/teams/{eventId}/{memberId}")
    public ResponseEntity<Void> deleteTeamMemberFromEvent(
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @PathVariable Long eventId,
            @PathVariable Long memberId) {
        log.debug("Deleting team member id = '{}' from team event id = '{}' by user id = '{}'", memberId, eventId, userId);
        teamMemberService.deleteTeamMemberFromEvent(userId, eventId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}