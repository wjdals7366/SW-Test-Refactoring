package com.yeom.pass.repository.user;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.yeom.pass.repository.BaseEntity;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.yeom.pass.repository.user.UserStatus.ACTIVE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class UserDto{

    private long id;

    @Size(min = 5, max = 20, message = "아이디는 5~20 자리로 입력해주세요.")
    private String userId;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$", message = "비밀번호는 영문과 특수문자, 숫자를 포함하며 8자 이상이어야 합니다.")
    private String password;

    private String userName;
    @Enumerated(EnumType.STRING)
    private UserStatus status = ACTIVE;
    private String phone;
    private String date;
    private String eMail;
    private String kaKaoId;

}
