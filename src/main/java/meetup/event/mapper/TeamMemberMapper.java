package meetup.event.mapper;

import meetup.event.dto.teammember.NewTeamMemberDto;
import meetup.event.dto.teammember.TeamMemberDto;
import meetup.event.dto.teammember.UpdateTeamMemberDto;
import meetup.event.model.teammember.TeamMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {
    @Mapping(target = "id", expression = "java(new TeamMemberId(newTeamMemberDto.eventId(), newTeamMemberDto.userId()))")
    @Mapping(target = "role", source = "role")
    TeamMember toTeamMember(NewTeamMemberDto newTeamMemberDto);

    @Mapping(source = "id.eventId", target = "eventId")
    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "role", target = "role")
    TeamMemberDto toTeamMemberDto(TeamMember teamMember);

    List<TeamMemberDto> toTeamMemberDtoList(List<TeamMember> teamMembers);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateTeamMember(UpdateTeamMemberDto updateTeamMemberDto, @MappingTarget TeamMember teamMemberToToUpdate);
}