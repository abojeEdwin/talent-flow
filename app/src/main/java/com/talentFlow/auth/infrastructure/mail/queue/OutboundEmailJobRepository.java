package com.talentFlow.auth.infrastructure.mail.queue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboundEmailJobRepository extends JpaRepository<OutboundEmailJob, UUID> {
    List<OutboundEmailJob> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            EmailJobStatus status,
            LocalDateTime nextAttemptAt
    );

    List<OutboundEmailJob> findTop20ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(
            EmailJobStatus status,
            LocalDateTime updatedAt
    );
}
