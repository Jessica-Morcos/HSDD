package org.hsdd.repo;

import org.hsdd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByActiveTrue();

    Optional<User> findByUsername(String username);

    // ⭐ ADD THIS
    List<User> findByRole(String role);
}
