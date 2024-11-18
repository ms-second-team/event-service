package meetup.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Location createLocation(Location location) {
        Location locationSaved = getLocationByLatAndLon(location.getLat(), location.getLon());

        if (locationSaved == null) {
            locationSaved = locationRepository.save(location);

            log.info("Location created - lat: " + location.getLat() + ", lon: " + location.getLon());
        } else {
            log.info("Location found - lat: " + location.getLat() + ", lon: " + location.getLon());
        }

        return locationSaved;
    }

    private Location getLocationByLatAndLon(Float lat, Float lon) {
        return locationRepository.findByLatAndLon(lat, lon).orElse(null);
    }

}