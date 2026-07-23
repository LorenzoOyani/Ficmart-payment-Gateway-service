package com.org.infrastructure.outbox;

import com.org.persistence.entities.OutboxEventEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripeWebhookOutboxFactory {


    private static final String TOPIC = "stripe.webhook";

    public OutboxEventEntity webhookEvent(UUID webhookId) {

        final String payload =
                """
               {"webhookEventId":"%s"}
               """.formatted(webhookId);


        return new OutboxEventEntity(
                "STRIPE_WEBHOOK",
                webhookId.toString(),
                "STRIPE_WEBHOOK_RECEIVED",
                TOPIC,
                webhookId.toString(),
                payload

        );
    }
}
