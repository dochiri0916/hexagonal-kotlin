package com.dochiri.hexagonal.domain.user.exception

import com.dochiri.hexagonal.domain.user.vo.Email

class DuplicateEmailException(email: Email) : UserException("이미 사용중인 이메일입니다: ${email.value}")