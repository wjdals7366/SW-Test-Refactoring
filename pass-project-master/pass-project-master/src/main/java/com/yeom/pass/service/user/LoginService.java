package com.yeom.pass.service.user;

import com.yeom.pass.repository.user.UserDto;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.repository.user.UserRepository;
import io.netty.handler.codec.base64.Base64Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.beans.Encoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    public void encryptPassword(UserEntity user) {

        String enPw = passwordEncoder.encode(user.getPassword());
        //user.setCreatedAt(LocalDateTime.now());
        user.setDate(LocalDateTime.now().toString());
        user.setPassword(enPw);

        userRepository.save(user);
    }

    public boolean create(UserDto userDto, Map<String, String> error_text) { // 회원가입

        UserEntity user = UserEntity.createUserEntity(userDto);

        if (user.getUserId() == "" || user.getPassword() == "" || user.getUserName() == "" || user.getEMail() == "" || user.getKaKaoId() == "" || user.getPassword() == "") {   //
            error_text.put("msg", "입력이 안된 사항이 있습니다.");
            return true;
        } else if (userRepository.findByUserId(user.getUserId()) == null) {

            try {
                encryptPassword(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            error_text.put("msg", "이미 존재하는 ID입니다.");
            return true;
        }
    }

    public boolean isUser(String id, String pw, Map<String, String> map) {   // 로그인 본인 확인 절차

        if (userRepository.findByUserId(id) == null) {
            map.put("msg", "존재하지 않는 ID입니다.");
            return false;
        } else {
            if (passwordEncoder.matches(pw, userRepository.findByUserId(id).getPassword()))
                return true;

            map.put("msg", "비밀번호가 일치하지 않습니다.");
            return false;
        }
    }

    public UserEntity getUser(UserDto userDto, Map<String, String> map) {
        if (isUser(userDto.getUserId(), userDto.getPassword(), map))
            return userRepository.findByUserId(userDto.getUserId());
        else
            return null;
    }
}
