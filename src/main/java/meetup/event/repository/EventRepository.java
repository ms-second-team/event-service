package meetup.event.repository;

import meetup.event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (:ownerId is null or e.ownerId = :ownerId)")
    List<Event> findAllByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);
}