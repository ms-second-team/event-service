package meetup.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import meetup.exception.ErrorResponse;
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
@Tag(name = "Event API")
public class EventController {
    private final EventService eventService;
    private final TeamMemberService teamMemberService;
    private final EventMapper eventMapper;
    private static final String HEADER_X_USER_ID = "X-User-Id";

    @Operation(summary = "Create new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New event was created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown exception", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping
    public ResponseEntity<EventDto> createEvent(@Parameter(description = "HEADER_X_USER_ID", required = true)
                                                @RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @RequestBody @Valid NewEventDto newEventDto) {

        log.info("---START CREATE EVENT ENDPOINT---");

        Event event = eventMapper.toEventFromNewEventDto(newEventDto);
        Event eventCreated = eventService.createEvent(userId, event);
        EventDto eventDto = eventMapper.toEventDto(eventCreated);

        return new ResponseEntity<>(eventDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Update event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event was updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "User is not the owner", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Event not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown exception", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@Parameter(description = "HEADER_X_USER_ID", required = true)
                                                @RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @Parameter(description = "Event to update id", required = true)
                                                @PathVariable Long id,
                                                @RequestBody @Valid UpdatedEventDto
                                                        updatedEventDto) {

        log.info("---START UPDATE EVENT ENDPOINT---");

        Event event = eventService.updateEvent(userId, id, updatedEventDto);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @Operation(summary = "Find event by id",
            description = "Find event by id. Event creation datetime is shown only when requester is the owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event was retrieved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Event not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@Parameter(description = "HEADER_X_USER_ID", required = true)
                                                 @RequestHeader(HEADER_X_USER_ID) Long userId,
                                                 @Parameter(description = "Event to find id", required = true)
                                                 @PathVariable Long id) {

        log.info("---START GET EVENT BY ID ENDPOINT---");

        Event event = eventService.getEventByEventId(id, userId);
        EventDto eventDto = eventMapper.toEventDto(event);

        return new ResponseEntity<>(eventDto, HttpStatus.OK);
    }

    @Operation(summary = "Find events by owner",
            description = "Find events by owner with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events were retrieved", content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EventDto.class)))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@Parameter(description = "HEADER_X_USER_ID", required = true)
                                                    @RequestParam(required = false) Long userId,
                                                    @Parameter(description = "Page number")
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @Parameter(description = "Page size")
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("---START GET EVENTS ENDPOINT---");

        List<Event> events = eventService.getEvents(from, size, userId);
        List<EventDto> eventsDto = eventMapper.toDtoList(events);

        return new ResponseEntity<>(eventsDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete event by id",
            description = "Delete event by id, Only owner is authorized")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event was deleted"),
            @ApiResponse(responseCode = "403", description = "User is not the owner", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Event not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@Parameter(description = "HEADER_X_USER_ID", required = true)
                                                @RequestHeader(HEADER_X_USER_ID) Long userId,
                                                @Parameter(description = "Event to find id", required = true)
                                                @PathVariable Long id) {

        log.info("---START DELETE EVENT BY ID ENDPOINT--");

        eventService.deleteEventById(userId, id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Add team member",
            description = "Add team member to event by owner or manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team member was added", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "User is not the owner or manager of event", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Team member or event not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/teams")
    public ResponseEntity<TeamMemberDto> addTeamMember(
            @Parameter(description = "HEADER_X_USER_ID", required = true)
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @RequestBody @Valid NewTeamMemberDto newTeamMemberDto) {
        log.debug("Creating team member id = '{}' event id = '{}' by user id = '{}'",
                newTeamMemberDto.userId(), newTeamMemberDto.eventId(), userId);
        TeamMemberDto teamMemberDto = teamMemberService.addTeamMember(userId, newTeamMemberDto);
        return new ResponseEntity<>(teamMemberDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get team members by event id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team members were retrieved", content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TeamMemberDto.class)))
            }),
            @ApiResponse(responseCode = "404", description = "Event was not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/teams/{eventId}")
    public ResponseEntity<List<TeamMemberDto>> getTeamsByEventId(
            @Parameter(description = "HEADER_X_USER_ID", required = true)
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @Parameter(description = "Id of event to which the team belongs", required = true)
            @PathVariable Long eventId) {
        log.debug("User id = '{}' requests team info event id = '{}'", userId, eventId);
        List<TeamMemberDto> teamMemberDtos = teamMemberService.getTeamsByEventId(userId, eventId);
        return new ResponseEntity<>(teamMemberDtos, HttpStatus.OK);
    }

    @Operation(summary = "Update team member in event",
            description = "Change team members role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team member was updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TeamMemberDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "User is not the owner or manager of event", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Team member or event not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PatchMapping("/teams/{eventId}/{memberId}")
    public ResponseEntity<TeamMemberDto> updateTeamMemberInEvent(
            @Parameter(description = "HEADER_X_USER_ID", required = true)
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @Parameter(description = "Event id", required = true)
            @PathVariable Long eventId,
            @Parameter(description = "Team member id", required = true)
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateTeamMemberDto updateTeamMemberDto) {
        log.debug("Updating team member id = '{}' in team event id = '{}' by user id = '{}'", memberId, eventId, userId);
        TeamMemberDto teamMemberDto = teamMemberService.updateTeamMemberInEvent(userId, eventId, memberId, updateTeamMemberDto);
        return new ResponseEntity<>(teamMemberDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete team member from event",
            description = "Delete team member from event, Only owner or manager is authorized")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Team member was deleted"),
            @ApiResponse(responseCode = "403", description = "User is not the owner or manager", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Event or team member was not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "500", description = "Unknown error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/teams/{eventId}/{memberId}")
    public ResponseEntity<Void> deleteTeamMemberFromEvent(
            @Parameter(description = "HEADER_X_USER_ID", required = true)
            @RequestHeader(HEADER_X_USER_ID) Long userId,
            @Parameter(description = "Event id", required = true)
            @PathVariable Long eventId,
            @Parameter(description = "Team member id", required = true)
            @PathVariable Long memberId) {
        log.debug("Deleting team member id = '{}' from team event id = '{}' by user id = '{}'", memberId, eventId, userId);
        teamMemberService.deleteTeamMemberFromEvent(userId, eventId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}