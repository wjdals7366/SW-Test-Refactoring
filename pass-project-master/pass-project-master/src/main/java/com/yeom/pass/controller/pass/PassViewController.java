package com.yeom.pass.controller.pass;

import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.instructor.InstructDto;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.service.instructor.InstructorService;
import com.yeom.pass.service.pass.Pass;
import com.yeom.pass.service.pass.PassService;
import com.yeom.pass.service.user.User;
import com.yeom.pass.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/passes")
public class PassViewController {
    private final UserService userService;
    private final PassService passService;
    private final InstructorService instructorService;
    private final HttpSession session;

    public PassViewController(UserService userService, PassService passService, InstructorService instructorService, HttpSession httpSession) {
        this.userService = userService;
        this.passService = passService;
        this.instructorService = instructorService;
        this.session = httpSession;
    }

    @GetMapping
    public ModelAndView getPasses(@RequestParam(value = "userId", required = false) String userId, ModelAndView modelAndView) {
        List<Pass> passes = new ArrayList<>();
        UserEntity user = null;


        if(session.getAttribute("user") != null){
            user = (UserEntity) session.getAttribute("user");
            userId = user.getUserId();
            passes = passService.getPasses(userId);
        }

        modelAndView.addObject("passes", passes);
        modelAndView.addObject("user", user);
        modelAndView.setViewName("pass/index");


        return modelAndView;
    }

    @GetMapping("/reserve")    // 이용권 클릭 시
    public ModelAndView reservePass(@RequestParam("passSeq") Long passSeq, @RequestParam("userId") String userId, ModelAndView modelAndView) {
        modelAndView.addObject("passSeq", passSeq);
        modelAndView.addObject("userId", userId);
        modelAndView.setViewName("pass/calendar");

        return modelAndView;
    }

    @PostMapping("/check_booking")    // 예약 중복 체크
    @ResponseBody
    public ResponseEntity<Map> checkBooking(@RequestBody Map<String, String> data) {

        Boolean bookingIsUsed = passService.getBooking(data.get("userId"), Integer.parseInt(data.get("passSeq")));
        Map<String, Boolean> map = new HashMap<>();
        map.put("bookingIsUsed", bookingIsUsed);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/reserve_date")   // 날짜 선택 시
    @ResponseBody
    public List<InstructDto> reserveDate(@RequestBody Map<String, String> data) {

        String currentYear = data.get("year");
        String clickedMonth = data.get("month");
        String clickedDate = data.get("date");
        List<String> date = List.of("1","2","3","4","5","6","7","8","9");

        if(date.contains(clickedMonth)) clickedMonth = "0" + clickedMonth;
        if(date.contains(clickedDate)) clickedDate = "0" + clickedDate;


        System.out.println(clickedMonth + ',' + clickedDate);
        List<InstructDto> instructDtoList = instructorService.getInstructorsByDate(currentYear + "-" + clickedMonth + "-" + clickedDate);
        System.out.println(instructDtoList.toString());

        return instructDtoList; // 해당 날짜에 등록되어있는 강사들과 해당 강사의 비어있는 시간들을 반환
    }


    @PostMapping("/end_class")  //수업 종료
    @ResponseBody
    public ResponseEntity<Map> endClass(@RequestBody Map<String, String> data) {

        int bookingSeq = Integer.parseInt(data.get("bookingSeq"));

        if(passService.endBooking(bookingSeq) == 1){
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
