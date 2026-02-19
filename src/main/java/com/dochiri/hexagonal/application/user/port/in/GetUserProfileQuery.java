package com.dochiri.hexagonal.application.user.port.in;

import com.dochiri.hexagonal.application.user.dto.UserProfileResult;

public interface GetUserProfileQuery {

    UserProfileResult getProfile(String userPublicId);

}