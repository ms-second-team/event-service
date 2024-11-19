package meetup.event.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdDateTime,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location,
        Long ownerId
) {
}