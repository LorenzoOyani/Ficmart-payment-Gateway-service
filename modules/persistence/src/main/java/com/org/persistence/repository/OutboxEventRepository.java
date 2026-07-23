package com.org.persistence.repository;

import com.org.domain.model.OutboxEventStatus;
import com.org.persistence.entities.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    @Query("""
        select e from OutboxEventEntity e
        where e.status = com.org.domain.model.OutboxEventStatus.PENDING
        order by e.createdAt asc
    """)
    List<OutboxEventEntity> findPendingEvents(Pageable pageable);

    @Modifying
    @Query("""
        update OutboxEventEntity e
        set e.status = :status
        where e.id = :id
        and e.status = com.org.domain.model.OutboxEventStatus.PENDING
    """)
    int updateStatusIfPending(
            @Param("id") UUID id,
            @Param("status") OutboxEventStatus status
    );

    @Query("""
    select
""")
    List<OutboxEventEntity>findRetryableEvents(Pageable pageable);
}
