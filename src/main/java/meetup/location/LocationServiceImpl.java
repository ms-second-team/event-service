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
        Location locationSaved;
        if (!checkLocation(location)) {
            locationSaved = locationRepository.save(location);
            log.info("Location created - lat: " + location.getLat() + ", lon: " + location.getLon());
        } else {
            locationSaved = locationRepository.findByLatAndLon(location.getLat(), location.getLon());
            log.info("Location found - lat: " + location.getLat() + ", lon: " + location.getLon());
        }
        return locationSaved;
    }

    private Boolean checkLocation(Location location) {
        return locationRepository.findByLatAndLon(location.getLat(), location.getLon()) != null;
    }

}