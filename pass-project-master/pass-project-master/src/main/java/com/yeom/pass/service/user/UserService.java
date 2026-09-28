package com.yeom.pass.service.user;

import com.yeom.pass.repository.booking.BookingDto;
import com.yeom.pass.repository.booking.BookingEntity;
import com.yeom.pass.repository.booking.BookingRepository;
import com.yeom.pass.repository.user.UserEntity;
import com.yeom.pass.repository.user.UserGroupMappingEntity;
import com.yeom.pass.repository.user.UserGroupMappingRepository;
import com.yeom.pass.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final UserGroupMappingRepository userGroupMappingRepository;

    public UserEntity getUser(final String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        return userEntity;
    }

    public List<UserEntity> getUserList(){

        List<UserEntity> userEntityList = userRepository.findAll();

        for(UserEntity u : userEntityList){
            if(u.getDate() != null){
                String date = u.getDate().split("T")[0];
                u.setDate(date);
            }
        }

        return userEntityList;
    }

    public List<BookingDto> getBookingList(){
        List<BookingEntity> bookingEntities = bookingRepository.findAll();
        List<BookingDto> bookingList = new ArrayList<>();
        for(BookingEntity booking : bookingEntities){
            if(booking.isUsedPass()){
                BookingDto bookingDto = new BookingDto();
                bookingDto.setBookingSeq(booking.getBookingSeq());
                bookingDto.setDate(booking.getStartedAt().toString().split("T")[0]);
                bookingDto.setStartedAt(booking.getStartedAt().toString().split("T")[1]);
                bookingDto.setEndedAt(booking.getEndedAt().toString().split("T")[1]);
                bookingDto.setInstructorName(booking.getInstructorName());
                bookingDto.setUserName(booking.getUserEntity().getUserName());
                bookingDto.setUserId(booking.getUserEntity().getUserId());

                bookingList.add(bookingDto);
            }

        }
        return bookingList;
    }

    public void setUserGroup(UserEntity user){
        UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId("HANBADA");
        userGroupMappingEntity.setUserId(user.getUserId());
        userGroupMappingEntity.setUserGroupName("한바다");
        userGroupMappingEntity.setDescription("한바다 임직원 그룹");

        userGroupMappingRepository.save(userGroupMappingEntity);
    }
}
