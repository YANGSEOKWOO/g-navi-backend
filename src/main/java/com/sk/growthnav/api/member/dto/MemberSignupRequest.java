package com.sk.growthnav.api.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberSignupRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 15, message = "이름은 15자 이하여야 합니다.")
    String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
    String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password;
}
