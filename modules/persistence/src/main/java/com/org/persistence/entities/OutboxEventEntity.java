package com.org.persistence.entities;

import com.org.domain.model.OutboxEventStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String eventKey;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    private int publishAttempts;

    private String failureReason;

    @Column(nullable = false)
    private final Instant createdAt = Instant.now();

    private Instant publishedAt;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String eventKey,
            String payload
    ) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.eventKey = eventKey;
        this.payload = payload;
    }

    public void markPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = OutboxEventStatus.FAILED;
        this.publishAttempts++;
        this.failureReason = reason;
    }

    public void markPendingRetry() {
        this.status = OutboxEventStatus.PENDING;
    }


}

