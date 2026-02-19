package com.dochiri.hexagonal.application.user.service.query

import com.dochiri.hexagonal.application.user.dto.UserProfileResult
import com.dochiri.hexagonal.application.user.port.`in`.GetUserProfileQuery
import com.dochiri.hexagonal.domain.user.vo.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserProfileService(
    private val userFinder: UserFinder
) : GetUserProfileQuery {

    @Transactional(readOnly = true)
    override fun getProfile(userPublicId: String): UserProfileResult =
        UserProfileResult.from(userFinder.findById(UserId.from(userPublicId)))
}
