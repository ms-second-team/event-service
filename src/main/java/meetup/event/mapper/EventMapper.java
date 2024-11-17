package meetup.event.mapper;

import meetup.event.dto.EventDto;
import meetup.event.dto.NewEventDto;
import meetup.event.model.Event;
import meetup.location.Location;
import meetup.location.LocationDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEventFromNewEventDto(NewEventDto newEventDto);

    EventDto toEventDto(Event event);

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);

    List<EventDto> toDtoList(List<Event> events);
}
