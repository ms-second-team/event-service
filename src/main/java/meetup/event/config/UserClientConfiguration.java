package meetup.event.config;

import feign.codec.ErrorDecoder;
import meetup.event.client.UserClientErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserClientConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserClientErrorDecoder();
    }
}