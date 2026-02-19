package com.dochiri.hexagonal.infrastructure.user.adapter.out.persistence;

import com.dochiri.hexagonal.application.user.port.out.LoadUserPort;
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort;
import com.dochiri.hexagonal.domain.user.User;
import com.dochiri.hexagonal.domain.user.vo.Email;
import com.dochiri.hexagonal.domain.user.vo.UserId;
import com.dochiri.hexagonal.infrastructure.user.entity.UserEntity;
import com.dochiri.hexagonal.infrastructure.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> loadById(UserId userId) {
        return userJpaRepository.findByPublicId(userId.value())
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> loadByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value())
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        Optional<UserEntity> existingOptional =
                userJpaRepository.findByPublicId(user.getId().value());

        if (existingOptional.isEmpty()) {
            UserEntity newEntity = userMapper.toEntity(user);
            UserEntity saved = userJpaRepository.save(newEntity);
            return userMapper.toDomain(saved);
        }

        UserEntity existing = existingOptional.get();
        userMapper.applyFullUpdate(user, existing);

        UserEntity updated = userJpaRepository.save(existing);
        return userMapper.toDomain(updated);
    }

}
