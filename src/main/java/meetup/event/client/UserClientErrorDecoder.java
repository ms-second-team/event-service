package meetup.event.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import meetup.exception.NotFoundException;

public class UserClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {

        switch (response.status()) {
            case 404:
                return new NotFoundException("User was not found");
            default:
                return new Exception("Unknown error");
        }
    }
}
