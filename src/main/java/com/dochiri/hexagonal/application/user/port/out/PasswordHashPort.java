package com.dochiri.hexagonal.application.user.port.out;

public interface PasswordHashPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

}
