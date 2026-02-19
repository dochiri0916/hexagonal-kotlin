package com.dochiri.hexagonal.application.user.service.command;

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand;
import com.dochiri.hexagonal.application.user.dto.LoginUserResult;
import com.dochiri.hexagonal.application.user.port.in.LoginUserUseCase;
import com.dochiri.hexagonal.application.user.port.out.JwtTokenPort;
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort;
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort;
import com.dochiri.hexagonal.application.user.service.query.UserFinder;
import com.dochiri.hexagonal.domain.user.User;
import com.dochiri.hexagonal.domain.user.exception.InactiveUserException;
import com.dochiri.hexagonal.domain.user.exception.InvalidPasswordException;
import com.dochiri.hexagonal.domain.user.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUserService implements LoginUserUseCase {

    private final UserFinder userFinder;
    private final SaveUserPort saveUserPort;
    private final PasswordHashPort passwordHashPort;
    private final JwtTokenPort jwtTokenPort;

    @Transactional
    @Override
    public LoginUserResult login(LoginUserCommand loginUserCommand) {
        Email email = Email.from(loginUserCommand.email());
        User user = userFinder.findByEmail(email);

        if (!passwordHashPort.matches(loginUserCommand.password(), user.getPasswordHash())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InactiveUserException("활성화되지 않은 계정입니다.");
        }

        User updatedUser = saveUserPort.save(user.updateLastLoginAt());

        String accessToken = jwtTokenPort.generateAccessToken(
                updatedUser.getId().value(),
                updatedUser.getRole().name()
        );

        return LoginUserResult.of(
                updatedUser.getId().value(),
                updatedUser.getEmail().value(),
                updatedUser.getRole().name(),
                accessToken
        );
    }

}
