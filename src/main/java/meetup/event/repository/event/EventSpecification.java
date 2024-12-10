package meetup.event.repository.event;

import lombok.experimental.UtilityClass;
import meetup.event.model.event.Event;
import meetup.event.model.event.EventRegistrationStatus;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class EventSpecification {

    public static Specification<Event> ownerIdEquals(Long userId) {
        if (userId == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ownerId"), userId);
    }

    public static Specification<Event> registrationStatusEquals(EventRegistrationStatus registrationStatus) {
        if (registrationStatus == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("registrationStatus"), registrationStatus);
    }
}