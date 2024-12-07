package meetup.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import meetup.event.dto.UserDto;
import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.model.event.Event;
import meetup.event.model.teammember.TeamMemberRole;
import meetup.exception.NotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "app.user-service.url=localhost:${wiremock.server.port}"
})
public class TeamMemberServiceIntegrateTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine");
    @Autowired
    private EventService eventService;
    @Autowired
    private TeamMemberService teamMemberService;
    private ObjectMapper objectMapper;
    private final Event event = Event.builder()
            .id(null)
            .name("event")
            .description("event description")
            .createdDateTime(null)
            .startDateTime(LocalDateTime.of(2024, 12, 26, 18, 0, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 26, 22, 0, 0))
            .location("location")
            .ownerId(null)
            .build();

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Test
    void addTeamMember() throws JsonProcessingException {
        Long userId = 1L;
        Long memberId = 2L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        eventService.createEvent(userId, event);

        UserDto memberDto = createUser(memberId);
        stubFor(get(urlEqualTo("/users/" + memberId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(memberDto))
                        .withStatus(HttpStatus.OK.value())));

        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(event.getId(), memberId, TeamMemberRole.MEMBER);

        TeamMemberDto teamMemberDto = teamMemberService.addTeamMember(userId, newTeamMemberDto);

        List<TeamMemberDto> teamMembersDto = teamMemberService.getTeamsByEventId(userId, event.getId());

        assertNotNull(teamMembersDto);
        assertEquals(teamMembersDto.size(), 1);
        assertEquals(teamMembersDto.getFirst(), teamMemberDto);
    }

    @Test
    void addTeamMemberWithNotExistUser() throws JsonProcessingException {
        Long userId = 1L;
        Long otherUserId = 2L;
        Long memberId = 3L;

        UserDto otherUserDto = createUser(otherUserId);
        stubFor(get(urlEqualTo("/users/" + otherUserId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(otherUserDto))
                        .withStatus(HttpStatus.OK.value())));

        eventService.createEvent(otherUserId, event);

        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        UserDto memberDto = createUser(memberId);
        stubFor(get(urlEqualTo("/users/" + memberId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(memberDto))
                        .withStatus(HttpStatus.OK.value())));

        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(event.getId(), memberId, TeamMemberRole.MEMBER);

        NotFoundException ex = Assert.assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("User was not found", ex.getLocalizedMessage());
    }

    @Test
    void addTeamMemberWithNotExistMember() throws JsonProcessingException {
        Long userId = 1L;
        Long memberId = 2L;

        UserDto userDto = createUser(userId);
        stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(objectMapper.writeValueAsString(userDto))
                        .withStatus(HttpStatus.OK.value())));

        eventService.createEvent(userId, event);

        stubFor(get(urlEqualTo("/users/" + memberId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        NewTeamMemberDto newTeamMemberDto = new NewTeamMemberDto(event.getId(), memberId, TeamMemberRole.MEMBER);

        NotFoundException ex = Assert.assertThrows(NotFoundException.class,
                () -> teamMemberService.addTeamMember(userId, newTeamMemberDto));

        assertEquals("User was not found", ex.getLocalizedMessage());
    }

    private UserDto createUser(long userId) {
        return new UserDto(
                userId,
                "John",
                "john@example.com",
                "StrongP@ss1",
                "Hello");
    }

}