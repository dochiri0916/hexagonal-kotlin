package com.dochiri.hexagonal.domain.user.exception

import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId

class UserNotFoundException private constructor(message: String) : UserException(message) {
    companion object {
        fun byId(userId: UserId) = UserNotFoundException("해당 사용자를 찾을 수 없습니다: ${userId.value}")

        fun byEmail(email: Email) = UserNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: ${email.value}")
    }
}
