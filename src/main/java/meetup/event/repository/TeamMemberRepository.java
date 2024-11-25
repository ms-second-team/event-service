package meetup.event.repository;

import meetup.event.model.teammember.TeamMember;
import meetup.event.model.teammember.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    Optional<TeamMember> findByIdEventIdAndIdUserId(Long eventId, Long userId);

    List<TeamMember> findAllByIdEventId(Long eventId);
}
