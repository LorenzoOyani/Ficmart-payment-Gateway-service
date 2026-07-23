package com.org.infrastructure.service;


import com.org.infrastructure.outbox.StripeWebhookOutboxFactory;
import com.org.persistence.entities.OutboxEventEntity;
import com.org.persistence.entities.StripeWebhookEntity;
import com.org.persistence.repository.OutboxEventRepository;
import com.org.persistence.repository.StripeWebhookRepository;
import com.stripe.model.Event;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void ingest(Event event, String rawPayload) {
        if (webhookRepository.existsByStripeEventId(event.getId())) {
            return;
        }

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
    }
}
