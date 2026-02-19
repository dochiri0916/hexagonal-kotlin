package com.dochiri.hexagonal.application.user.service.query;

import com.dochiri.hexagonal.application.user.dto.UserProfileResult;
import com.dochiri.hexagonal.application.user.port.in.GetUserProfileQuery;
import com.dochiri.hexagonal.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserProfileService implements GetUserProfileQuery {

    private final UserFinder userFinder;

    @Transactional(readOnly = true)
    @Override
    public UserProfileResult getProfile(String userPublicId) {
        return UserProfileResult.from(userFinder.findById(UserId.from(userPublicId)));
    }

}