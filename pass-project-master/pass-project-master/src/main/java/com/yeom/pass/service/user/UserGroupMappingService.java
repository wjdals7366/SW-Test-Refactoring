package com.yeom.pass.service.user;

import com.yeom.pass.repository.user.UserGroupMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserGroupMappingService {
    private final UserGroupMappingRepository userGroupMappingRepository;

    public UserGroupMappingService(UserGroupMappingRepository userGroupMappingRepository) {
        this.userGroupMappingRepository = userGroupMappingRepository;
    }

    public List<String> getAllUserGroupIds() {
        // user group id를 중복없이 user group id 순으로 조회합니다.
        return userGroupMappingRepository.findDistinctUserGroupId();

    }
}
