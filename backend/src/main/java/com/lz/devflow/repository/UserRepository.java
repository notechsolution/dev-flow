package com.lz.devflow.repository;

import com.lz.devflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String resetToken);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findById(String id);

    Page<User> findAll(Pageable pageable);

    Page<User> findByProjectIdsInAndRoleIsNot(List<String> projectIds, String role, Pageable pageable);
}
