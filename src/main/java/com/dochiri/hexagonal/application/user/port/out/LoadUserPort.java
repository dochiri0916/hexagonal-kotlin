package com.dochiri.hexagonal.application.user.port.out;

import com.dochiri.hexagonal.domain.user.User;
import com.dochiri.hexagonal.domain.user.vo.Email;
import com.dochiri.hexagonal.domain.user.vo.UserId;

import java.util.Optional;

public interface LoadUserPort {

    Optional<User> loadById(UserId userId);

    Optional<User> loadByEmail(Email email);

}