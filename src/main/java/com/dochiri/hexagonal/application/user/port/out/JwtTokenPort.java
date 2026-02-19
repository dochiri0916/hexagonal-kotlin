package com.dochiri.hexagonal.application.user.port.out;

public interface JwtTokenPort {

    String generateAccessToken(String userPublicId, String role);

    String generateRefreshToken(String userPublicId, String role);

    boolean validate(String token);

    String extractUserPublicId(String token);

    String extractRole(String token);

    boolean isExpired(String token);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);

}