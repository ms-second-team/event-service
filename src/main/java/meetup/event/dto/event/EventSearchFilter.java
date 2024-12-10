package meetup.event.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import meetup.event.model.event.EventRegistrationStatus;

@Builder
@Schema(description = "Search filter")
public record EventSearchFilter(
        @Schema(description = "Event owner id")
        Long userId,
        @Schema(description = "Event registration status")
        EventRegistrationStatus registrationStatus
) {
}