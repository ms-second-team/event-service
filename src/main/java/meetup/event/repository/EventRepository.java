package meetup.event.repository;

import meetup.event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOwnerId(Long ownerId, Pageable pageable);
}