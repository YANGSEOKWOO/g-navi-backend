package com.sk.growthnav.api.skill.dto;

import com.sk.growthnav.api.skill.entity.Skill;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillInfoDTO {

    Long skillId;
    String skillName;
    Long projectId;

    public static SkillInfoDTO from(Skill skill) {
        return SkillInfoDTO.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .projectId(skill.getProject().getId())
                .build();
    }
}
