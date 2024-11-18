package meetup.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l " +
            "FROM Location l " +
            "where l.lat = ?1 " +
            "and l.lon < ?2")
    Location findByLatAndLon(Float lat, Float lon);
}