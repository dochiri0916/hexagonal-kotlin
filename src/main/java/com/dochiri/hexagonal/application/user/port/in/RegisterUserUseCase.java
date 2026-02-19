package com.dochiri.hexagonal.application.user.port.in;

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand;
import com.dochiri.hexagonal.application.user.dto.RegisterUserResult;

public interface RegisterUserUseCase {

    RegisterUserResult register(RegisterUserCommand registerUserCommand);

}