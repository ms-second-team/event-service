package meetup.event.client;

import meetup.event.config.UserClientConfiguration;
import meetup.event.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "userClient", url = "${app.user-service.url}", configuration = UserClientConfiguration.class)
public interface UserClient {
    @GetMapping("/users/{id}")
    UserDto getUserById(@RequestHeader("X-User-Id") Long userId,
                        @PathVariable Long id);
}