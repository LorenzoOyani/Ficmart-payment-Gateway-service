package com.org.persistence.repository;

import com.org.domain.model.WebhookEventStatus;
import com.org.persistence.entities.StripeWebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StripeWebhookRepository extends JpaRepository<StripeWebhookEntity, UUID> {
    boolean existsByStripeEventId(String stripeEventId);
    Optional<StripeWebhookEntity> findByStripeEventId(String stripeEventId);

    boolean existsByProviderAndExternalEventId(String stripe, String id);


    @Modifying
    @Query("""
        update StripeWebhookEntity e
        set e.status = com.org.domain.model.WebhookEventStatus.PROCESSING
        where e.id = :id
        and e.status in :status
    """)
    int markProcessing(@Param("id") UUID id,@Param("status") Iterable<WebhookEventStatus> status);
}
