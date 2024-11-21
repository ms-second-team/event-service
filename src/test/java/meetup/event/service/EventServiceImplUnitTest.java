package meetup.event.service;

import meetup.event.dto.UpdatedEventDto;
import meetup.event.mapper.EventMapper;
import meetup.event.model.Event;
import meetup.event.repository.EventRepository;
import meetup.exception.NotAuthorizedException;
import meetup.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplUnitTest {
    @Mock
    private EventRepository repository;

    @Mock
    private EventMapper mapper;

    @InjectMocks
    private EventServiceImpl service;

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

    private final Long eventId = 1L;

    private final Long userId = 1L;

    @Captor
    private ArgumentCaptor<Event> captor;

    @Test
    void createEvent() {
        when(repository.save(any()))
                .thenReturn(event);

        service.createEvent(userId, event);

        verify(repository).save(captor.capture());
        Event eventToSave = captor.getValue();

        assertThat(eventToSave.getOwnerId(), is(userId));

        verify(repository, times(1)).save(eventToSave);
    }

    @Test
    void updateEvent() {
        UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
                .name(null)
                .description("updated description")
                .startDateTime(null)
                .endDateTime(LocalDateTime.of(2024, 12, 26, 23, 0, 0))
                .build();

        when(repository.findById(eventId))
                .thenReturn(Optional.of(event));
        doNothing().when(mapper).updateEvent(updatedEventDto, event);
        when(repository.save(event))
                .thenReturn(event);
        event.setOwnerId(userId);

        service.updateEvent(eventId, userId, updatedEventDto);

        verify(repository, times(1)).findById(eventId);
        verify(mapper, times(1)).updateEvent(updatedEventDto, event);
        verify(repository, times(1)).save(event);
    }

    @Test
    void updateEventByOtherUser() {
        UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
                .name(null)
                .description("updated description")
                .startDateTime(null)
                .endDateTime(LocalDateTime.of(2024, 12, 26, 23, 0, 0))
                .build();

        when(repository.findById(eventId))
                .thenReturn(Optional.of(event));

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> service.updateEvent(eventId, userId, updatedEventDto));

        assertThat(ex.getMessage(), is("User id=" + userId + " is not the owner of the event id=" + event.getId()));

        verify(repository, times(1)).findById(eventId);
        verify(mapper, never()).updateEvent(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updateNonExistEvent() {
        UpdatedEventDto updatedEventDto = UpdatedEventDto.builder()
                .name(null)
                .description("updated description")
                .startDateTime(null)
                .endDateTime(LocalDateTime.of(2024, 12, 26, 23, 0, 0))
                .build();

        when(repository.findById(eventId))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.updateEvent(eventId, userId, updatedEventDto));

        assertThat(ex.getMessage(), is("Event with id=" + eventId + " was not found"));

        verify(repository, times(1)).findById(eventId);
        verify(mapper, never()).updateEvent(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void getEventByEventId() {
        when(repository.findById(eventId))
                .thenReturn(Optional.of(event));

        service.getEventByEventId(eventId, userId);

        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void getNonExistEventByEventId() {
        when(repository.findById(eventId))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getEventByEventId(eventId, userId));

        assertThat(ex.getMessage(), is("Event with id=" + eventId + " was not found"));

        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void getEventsWithUserId() {
        List<Event> events = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllByOwnerId(userId, pageable))
                .thenReturn(events);

        service.getEvents(0, 10, userId);

        verify(repository, times(1)).findAllByOwnerId(userId, PageRequest.of(0, 10));
    }

    @Test
    void getEventsWithoutUserId() {
        List<Event> events = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllByOwnerId(null, pageable))
                .thenReturn(events);

        service.getEvents(0, 10, null);

        verify(repository, times(1)).findAllByOwnerId(null, PageRequest.of(0, 10));
    }

    @Test
    void deleteEventById() {
        event.setOwnerId(userId);

        when(repository.findById(eventId))
                .thenReturn(Optional.of(event));

        service.deleteEventById(eventId, userId);

        verify(repository, times(1)).deleteById(eventId);
    }

    @Test
    void deleteNonExistEvent() {
        when(repository.findById(eventId))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteEventById(eventId, userId));

        assertThat(ex.getMessage(), is("Event with id=" + eventId + " was not found"));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteEventByOtherUser() {
        when(repository.findById(eventId))
                .thenReturn(Optional.of(event));

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> service.deleteEventById(eventId, userId));

        assertThat(ex.getMessage(), is("User id=" + userId + " is not the owner of the event id=" + event.getId()));

        verify(repository, never()).deleteById(any());

    }

}