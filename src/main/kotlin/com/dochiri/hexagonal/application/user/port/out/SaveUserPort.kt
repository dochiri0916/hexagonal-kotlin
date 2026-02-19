package com.dochiri.hexagonal.application.user.port.out

import com.dochiri.hexagonal.domain.user.User

interface SaveUserPort {
    fun save(user: User): User
}
