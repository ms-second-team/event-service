package meetup.event.mapper;

import meetup.event.dto.EventDto;
import meetup.event.dto.NewEventDto;
import meetup.event.dto.UpdatedEventDto;
import meetup.event.model.Event;
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