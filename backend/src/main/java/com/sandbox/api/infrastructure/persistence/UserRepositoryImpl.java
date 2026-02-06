package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.User;
import com.sandbox.api.domain.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** MyBatis implementation of the UserRepository interface. */
@Repository
public class UserRepositoryImpl implements UserRepository {

  private final UserMapper userMapper;

  public UserRepositoryImpl(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(userMapper.findByUsername(username));
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userMapper.findById(id));
  }

  @Override
  public boolean existsByUsername(String username) {
    return userMapper.existsByUsername(username);
  }
}
