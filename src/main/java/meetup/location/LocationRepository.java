package meetup.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l " +
            "FROM Location l " +
            "WHERE l.lat = ?1 " +
            "AND l.lon = ?2")
    Optional<Location> findByLatAndLon(Float lat, Float lon);
}