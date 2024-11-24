package meetup.event.mapper;

import meetup.event.dto.NewTeamMemberDto;
import meetup.event.dto.TeamMemberDto;
import meetup.event.dto.UpdateTeamMemberDto;
import meetup.event.model.TeamMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {
    TeamMember toTeamMember(NewTeamMemberDto newTeamMemberDto);

    TeamMemberDto toTeamMemberDto(TeamMember teamMember);

    List<TeamMemberDto> toTeamMemberDtoList(List<TeamMember> teamMembers);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateTeamMember(UpdateTeamMemberDto updateTeamMemberDto, @MappingTarget TeamMember teamMemberToToUpdate);
}
