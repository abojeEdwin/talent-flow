package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE lower(u.email) LIKE lower(concat('%', :query, '%'))
               OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
               OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
            """)
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);
}
