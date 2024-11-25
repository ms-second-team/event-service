package meetup.event.mapper;

import meetup.event.dto.event.EventDto;
import meetup.event.dto.event.NewEventDto;
import meetup.event.dto.event.UpdatedEventDto;
import meetup.event.model.event.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEventFromNewEventDto(NewEventDto newEventDto);

    EventDto toEventDto(Event event);

    List<EventDto> toDtoList(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEvent(UpdatedEventDto updatedEventDto, @MappingTarget Event eventToToUpdate);
}