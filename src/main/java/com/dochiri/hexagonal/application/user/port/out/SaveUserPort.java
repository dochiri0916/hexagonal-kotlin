package com.dochiri.hexagonal.application.user.port.out;

import com.dochiri.hexagonal.domain.user.User;

public interface SaveUserPort {

    User save(User user);

}