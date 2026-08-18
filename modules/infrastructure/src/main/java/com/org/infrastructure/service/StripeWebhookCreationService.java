package com.org.infrastructure.service;


import com.org.infrastructure.outbox.StripeWebhookOutboxFactory;
import com.org.persistence.entities.OutboxEventEntity;
import com.org.persistence.entities.StripeWebhookEntity;
import com.org.persistence.repository.OutboxEventRepository;
import com.org.persistence.repository.StripeWebhookRepository;
import com.stripe.model.Event;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookCreationService {
    private final StripeWebhookRepository webhookRepository;
    private final OutboxEventRepository outboxRepository;
    private final StripeWebhookOutboxFactory outboxFactory;

    public StripeWebhookCreationService(
            StripeWebhookRepository webhookRepository,
            OutboxEventRepository outboxRepository,
            StripeWebhookOutboxFactory outboxFactory
    ) {
        this.webhookRepository = webhookRepository;
        this.outboxRepository = outboxRepository;
        this.outboxFactory = outboxFactory;
    }

    public enum INGESTRESULT{
        CREATED,
        DUPLICATE
    }

    @Transactional
    public INGESTRESULT ingest(Event event, String rawPayload) {
        validate(event, rawPayload);

        try {
            /// persist to db before queue
            StripeWebhookEntity storedWebhook = webhookRepository.save(
                    new StripeWebhookEntity(
                            event.getId(),
                            event.getType(),
                            rawPayload
                    )
            );

            OutboxEventEntity outboxEvent =
                    outboxFactory.webhookEvent(storedWebhook.getId());


            outboxRepository.save(outboxEvent);
            return INGESTRESULT.CREATED;
        } catch (DataIntegrityViolationException e) {
           return INGESTRESULT.DUPLICATE;
        }
    }

    private void validate(Event event, String rawPayload) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }
        if (event.getId() == null || event.getId().isBlank()) {
            throw new IllegalArgumentException("Stripe event id is required");
        }

        if (event.getType() == null || event.getType().isBlank()) {
            throw new IllegalArgumentException("Stripe event type is required");
        }

        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Stripe raw payload is required");
        }
    }
}
