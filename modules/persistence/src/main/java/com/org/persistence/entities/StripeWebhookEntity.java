package com.org.persistence.entities;

import com.org.domain.model.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter

@Entity
@Table(
        name = "stripe_webhook_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stripe_event_id",
                columnNames = "stripe_event_id"
        )
)
public class StripeWebhookEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "stripe_event_id", nullable = false, unique = true)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.org.domain.model.WebhookEventStatus status = com.org.domain.model.WebhookEventStatus.RECEIVED;

    private String failureReason;
    private int retryCount;

    private Instant receivedAt = Instant.now();
    private Instant processedAt;

    protected StripeWebhookEntity() {
    }

    public StripeWebhookEntity(String stripeEventId, String eventType, String payload) {
        this.stripeEventId = stripeEventId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = WebhookEventStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
    }


}
