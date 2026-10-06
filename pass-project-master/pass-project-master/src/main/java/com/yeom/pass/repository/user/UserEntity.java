package com.yeom.pass.repository.user;

import com.yeom.pass.repository.BaseEntity;
import lombok.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.yeom.pass.repository.user.UserStatus.ACTIVE;

@Data
@ToString(callSuper = true)
@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = true)    // 해당 클래스가 상속받은 부모 클래스의 필드를 가져옴
public class UserEntity{

    @Id
    private String userId;

    private String password;

    private String userName;

    @Enumerated(EnumType.STRING)
    private UserStatus status = ACTIVE;
    private String phone;

    private String eMail;

    private String date;

    private String kaKaoId;

    public static UserEntity createUserEntity(UserDto userDto){
        return new UserEntity(
                userDto.getUserId(),
                userDto.getPassword(),
                userDto.getUserName(),
                userDto.getStatus(),
                userDto.getPhone(),
                userDto.getEMail(),
                userDto.getDate(),
                userDto.getKaKaoId()
        );
    }

}