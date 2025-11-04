package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void save(User user);

    void delete(UserId userId);
}
