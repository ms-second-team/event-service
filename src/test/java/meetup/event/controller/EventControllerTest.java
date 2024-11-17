package meetup.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.event.service.EventService;
import meetup.location.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
class EventControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private EventService eventService;

    @Autowired
    private MockMvc mvc;

    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    private final Location location = new Location(1L, "Локация №1", 55.7855F, 37.7610F);

    private final Event event = new Event(
            1L, "Событие №1", "Описание события №1",
            LocalDateTime.of(2024, 11, 16, 13, 00, 00),
            LocalDateTime.of(2024, 12, 23, 18, 00, 00),
            LocalDateTime.of(2024, 12, 23, 22, 00, 00),
            location, 1L);

    @Test
    void createEvent() throws Exception {
        when(eventService.createEvent(anyLong(), any()))
                .thenReturn(event);

        mvc.perform(post("/events")
                        .content(mapper.writeValueAsString(event))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isCreated());
    }

    @Test
    void updateEvent() throws Exception {
        when(eventService.updateEvent(anyLong(), anyLong(), any()))
                .thenReturn(event);

        mvc.perform(patch("/events/1")
                        .content(mapper.writeValueAsString(event))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getEventById() throws Exception {
        when(eventService.getEventById(anyLong(), anyLong()))
                .thenReturn(event);

        mvc.perform(get("/events/1")
                        .content(mapper.writeValueAsString(event))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getEvents() throws Exception {
        when(eventService.getEventById(anyLong(), anyLong()))
                .thenReturn(event);

        mvc.perform(get("/events?userId=1&from=0&size=1000")
                        .content(mapper.writeValueAsString(event))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEventById() throws Exception {

        mvc.perform(delete("/events/1")
                        .content(mapper.writeValueAsString(event))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SHARER_USER_ID, 1))
                .andExpect(status().isNoContent());
    }

}