package meetup.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
public record ErrorResponse(String error) {

}