package com.talentFlow.auth.infrastructure.repository;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID>, UserRepositoryCustom {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(RoleName role, Pageable pageable);

    Page<User> findByRoleAndStatus(RoleName role, UserStatus status, Pageable pageable);

    @Query("""
            { $or: [
              { email: { $regex: ?0, $options: 'i' } },
              { firstName: { $regex: ?0, $options: 'i' } },
              { lastName: { $regex: ?0, $options: 'i' } }
            ] }
            """)
    Page<User> searchByQuery(String query, Pageable pageable);

    @Query("""
            { role: ?0, $or: [
              { email: { $regex: ?1, $options: 'i' } },
              { firstName: { $regex: ?1, $options: 'i' } },
              { lastName: { $regex: ?1, $options: 'i' } }
            ] }
            """)
    Page<User> searchByRoleAndQuery(RoleName role, String query, Pageable pageable);

    @Query("""
            { role: ?0, status: ?1, $or: [
              { email: { $regex: ?2, $options: 'i' } },
              { firstName: { $regex: ?2, $options: 'i' } },
              { lastName: { $regex: ?2, $options: 'i' } }
            ] }
            """)
    Page<User> searchByRoleAndStatusAndQuery(RoleName role, UserStatus status, String query, Pageable pageable);

    @Query("""
            { status: 'ACTIVE', $or: [
              { email: { $regex: ?0, $options: 'i' } },
              { firstName: { $regex: ?0, $options: 'i' } },
              { lastName: { $regex: ?0, $options: 'i' } }
            ] }
            """)
    Page<User> searchActiveUsersByQuery(String query, Pageable pageable);
}