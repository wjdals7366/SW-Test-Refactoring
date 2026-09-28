package com.yeom.pass.controller.admin;

import com.yeom.pass.repository.booking.BookingDto;
import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.service.packaze.PackageService;
import com.yeom.pass.service.pass.BulkPassService;
import com.yeom.pass.service.statistics.StatisticsService;
import com.yeom.pass.service.user.User;
import com.yeom.pass.service.user.UserGroupMappingService;
import com.yeom.pass.service.user.UserService;
import com.yeom.pass.util.LocalDateTimeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

import java.time.LocalDateTime;

@Controller
@RequestMapping(value = "/admin")
public class AdminViewController {
    private final BulkPassService bulkPassService;
    private final PackageService packageService;
    private final UserGroupMappingService userGroupMappingService;
    private final StatisticsService statisticsService;
    private final UserService userService;

    public AdminViewController(BulkPassService bulkPassService, PackageService packageService, UserGroupMappingService userGroupMappingService,
                               StatisticsService statisticsService, UserService userService) {
        this.bulkPassService = bulkPassService;
        this.packageService = packageService;
        this.userGroupMappingService = userGroupMappingService;
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView home(ModelAndView modelAndView, @RequestParam(value = "to", required = false) String toString) {
        LocalDateTime to = (toString == null) ? LocalDateTime.now() : LocalDateTimeUtils.parseDate(toString);

        // chartData 조회 (라벨, 출석횟수, 취소 횟수)
        modelAndView.addObject("chartData", statisticsService.makeChartData(to));
        modelAndView.setViewName("admin/index");
        return modelAndView;
    }

    @GetMapping("/chart")
    public String showChart(Model model) {
        List<String> labels = Arrays.asList("Red", "Blue", "Yellow");
        List<Integer> data = Arrays.asList(10, 20, 30);
        List<String> backgroundColor = Arrays.asList("#ff6384", "#36a2eb", "#ffce56");
        model.addAttribute("labels", labels);
        model.addAttribute("data", data);
        model.addAttribute("backgroundColor", backgroundColor);
        return "admin/chart";
    }

    @GetMapping("/bulk-pass")
    public ModelAndView registerBulkPass(ModelAndView modelAndView) {
        modelAndView.addObject("bulkPasses", bulkPassService.getAllBulkPasses());
        modelAndView.addObject("packages", packageService.getAllPackages());
        modelAndView.addObject("userGroupIds", userGroupMappingService.getAllUserGroupIds());
        modelAndView.addObject("request", new BulkPassRequest());
        modelAndView.setViewName("admin/bulk-pass");

        return modelAndView;
    }

    @PostMapping("/bulk-pass")
    public String addBulkPass(@ModelAttribute("request") BulkPassRequest request) {
        bulkPassService.addBulkPass(request);
        return "redirect:/admin/bulk-pass";
    }

    @GetMapping("/user")
    public ModelAndView userInfo(ModelAndView modelAndView){
        List<UserEntity> userList = userService.getUserList();
        List<BookingDto> bookingList = userService.getBookingList();

        modelAndView.addObject("userInfo", userList);
        modelAndView.addObject("userNum", userList.size());
        modelAndView.addObject("bookingInfo", bookingList);
        modelAndView.setViewName("admin/user");
        return modelAndView;
    }
}