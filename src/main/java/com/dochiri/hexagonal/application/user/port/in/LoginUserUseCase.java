package com.dochiri.hexagonal.application.user.port.in;

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand;
import com.dochiri.hexagonal.application.user.dto.LoginUserResult;

public interface LoginUserUseCase {

    LoginUserResult login(LoginUserCommand loginUserCommand);

}