package com.dochiri.hexagonal.application.user.service.command;

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand;
import com.dochiri.hexagonal.application.user.dto.RegisterUserResult;
import com.dochiri.hexagonal.application.user.port.in.RegisterUserUseCase;
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort;
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort;
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort;
import com.dochiri.hexagonal.domain.user.User;
import com.dochiri.hexagonal.domain.user.exception.DuplicateEmailException;
import com.dochiri.hexagonal.domain.user.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordHashPort passwordHashPort;

    @Transactional
    @Override
    public RegisterUserResult register(RegisterUserCommand registerUserCommand) {
        Email email = Email.from(registerUserCommand.email());

        loadUserPort.loadByEmail(email)
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(email.value());
                });

        String passwordHash = passwordHashPort.encode(registerUserCommand.password());

        User newUser = User.register(email, passwordHash, registerUserCommand.name());

        User savedUser = saveUserPort.save(newUser);

        return RegisterUserResult.of(
                savedUser.getId().value(),
                savedUser.getEmail().value()
        );
    }

}
