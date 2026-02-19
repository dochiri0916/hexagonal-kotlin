package com.dochiri.hexagonal.application.user.service.query

import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.domain.user.exception.UserNotFoundException
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFinder(
    private val loadUserPort: LoadUserPort
) {

    @Transactional(readOnly = true)
    fun findById(userId: UserId): User =
        loadUserPort.findById(userId) ?: throw UserNotFoundException.byId(userId)

    @Transactional(readOnly = true)
    fun findByEmail(email: Email): User =
        loadUserPort.findByEmail(email) ?: throw UserNotFoundException.byEmail(email)
}
