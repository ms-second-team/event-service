package meetup.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String error,
        Integer status
) {
}