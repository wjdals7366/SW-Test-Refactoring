package com.yeom.pass.service.user;

import com.yeom.pass.repository.user.UserDto;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.repository.user.UserStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class User {

    private String userId;
    private String userName;
    private UserStatus status;
    private String phone;

    public User toUser(UserEntity userEntity){
        this.userId = userEntity.getUserId();
        this.userName = userEntity.getUserName();
        this.status = userEntity.getStatus();
        this.phone = userEntity.getPhone();
        return this;
    }


}
