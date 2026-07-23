package com.org.infrastructure.stripe;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripeWebhookPublisher {

    public static final String TOPIC = "stripe.webhooks";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public StripeWebhookPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UUID webhookEventId) {
        kafkaTemplate.send(TOPIC, webhookEventId.toString(), webhookEventId.toString());
    }
}

