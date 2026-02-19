package com.dochiri.hexagonal.application.user.port.out

import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId

interface LoadUserPort {
    fun findById(userId: UserId): User?
    fun findByEmail(email: Email): User?
}
