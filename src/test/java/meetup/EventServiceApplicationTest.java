package meetup;

import meetup.event.controller.EventController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class EventServiceApplicationTest {
    @Autowired
    private EventController eventController;

    @Test
    public void contextLoads() {
        Assertions.assertThat(eventController).isNotNull();
    }
}