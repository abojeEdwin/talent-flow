package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
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

    Page<User> findByRole(RoleName role, Pageable pageable);

    Page<User> findByRoleAndStatus(RoleName role, UserStatus status, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE lower(u.email) LIKE lower(concat('%', :query, '%'))
               OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
               OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
            """)
    Page<User> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND NOT EXISTS (
                    SELECT tm FROM TeamMember tm
                    WHERE tm.user = u
              )
            """)
    Page<User> findUnallocatedInterns(@Param("role") RoleName role, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND u.status = :status
              AND NOT EXISTS (
                    SELECT tm FROM TeamMember tm
                    WHERE tm.user = u
              )
            """)
    Page<User> findUnallocatedInternsByStatus(@Param("role") RoleName role,
                                              @Param("status") UserStatus status,
                                              Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND NOT EXISTS (
                    SELECT tm FROM TeamMember tm
                    WHERE tm.user = u
              )
              AND (
                    lower(u.email) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
              )
            """)
    Page<User> searchUnallocatedInternsByQuery(@Param("role") RoleName role,
                                               @Param("query") String query,
                                               Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND (
                    lower(u.email) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
              )
            """)
    Page<User> searchByRoleAndQuery(@Param("role") RoleName role, @Param("query") String query, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND u.status = :status
              AND NOT EXISTS (
                    SELECT tm FROM TeamMember tm
                    WHERE tm.user = u
              )
              AND (
                    lower(u.email) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
              )
            """)
    Page<User> searchUnallocatedInternsByStatusAndQuery(@Param("role") RoleName role,
                                                        @Param("status") UserStatus status,
                                                        @Param("query") String query,
                                                        Pageable pageable);

@Query("""
            SELECT u FROM User u
            WHERE u.role = :role
            AND u.status = :status
            AND (
                    lower(u.email) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
            )
            """)
    Page<User> searchByRoleAndStatusAndQuery(@Param("role") RoleName role,
                                             @Param("status") UserStatus status,
                                             @Param("query") String query,
                                             Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.status = 'ACTIVE'
            AND (
                    lower(u.email) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.firstName) LIKE lower(concat('%', :query, '%'))
                 OR lower(u.lastName) LIKE lower(concat('%', :query, '%'))
            )
            ORDER BY u.firstName, u.lastName
            """)
    Page<User> searchActiveUsersByQuery(@Param("query") String query, Pageable pageable);
}
