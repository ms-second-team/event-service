package meetup.event.model.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String location;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "registration_status")
    @Enumerated(EnumType.STRING)
    private EventRegistrationStatus registrationStatus;
}