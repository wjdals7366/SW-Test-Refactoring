package com.yeom.pass.controller.login;

import com.yeom.pass.repository.user.UserDto;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.service.user.LoginService;
import com.yeom.pass.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private HttpSession session;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserService userService;

    @RequestMapping({"/", ""})
    public String Login(){ return "/login/login"; };


    @PostMapping("/action")
    @ResponseBody
    public ResponseEntity<Map<String, String>> LoginAction(@RequestBody UserDto userDto) {

        Map<String, String> error_text = new HashMap<>();

        if (loginService.isUser(userDto.getUserId(), userDto.getPassword(), error_text) == false)
            return new ResponseEntity<>(error_text, HttpStatus.BAD_REQUEST);

        UserEntity user = loginService.getUser(userDto, error_text);

        session.setAttribute("user", user);

        session.setMaxInactiveInterval(7200);

        userService.setUserGroup(user); // 그룹 생성

        //user Id와 password의 원문이 노출되는 문제 구문
        //Map<String, String> userDtoMap = new HashMap<>();
        //userDtoMap.put("userId", userDto.getUserId());
        //userDtoMap.put("password", userDto.getPassword());
        //return new ResponseEntity<>(userDtoMap, HttpStatus.OK);

        // 빈 구문을 둘려주는 방식으로 수정 (JSON 파일을 받지 않으면 오류처리되게 구현했기 때문임)
        return new ResponseEntity<>(new HashMap<String, String>(), HttpStatus.OK);

    }

    @RequestMapping("/logout")
    public String LoginAction(){
        session.invalidate();

        return "redirect:/login";
    }


}
