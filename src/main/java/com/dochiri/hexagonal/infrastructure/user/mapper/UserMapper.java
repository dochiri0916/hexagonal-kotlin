package com.dochiri.hexagonal.infrastructure.user.mapper;

import com.dochiri.hexagonal.domain.user.User;
import com.dochiri.hexagonal.domain.user.vo.Email;
import com.dochiri.hexagonal.domain.user.vo.UserId;
import com.dochiri.hexagonal.infrastructure.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getId().value(),
                domain.getEmail().value(),
                domain.getPasswordHash(),
                domain.getName(),
                domain.getStatus().name(),
                domain.getRole().name(),
                domain.getLastLoginAt()
        );
    }

    public User toDomain(UserEntity entity) {
        return User.of(
                UserId.from(entity.getPublicId()),
                Email.from(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getName(),
                UserStatus.valueOf(entity.getStatus()),
                UserRole.valueOf(entity.getRole()),
                entity.getLastLoginAt()
        );
    }

    public void applyFullUpdate(User domain, UserEntity entity) {
        entity.updateFromDomain(
                domain.getEmail().value(),
                domain.getPasswordHash(),
                domain.getName(),
                domain.getStatus().name(),
                domain.getRole().name(),
                domain.getLastLoginAt()
        );
    }

}
