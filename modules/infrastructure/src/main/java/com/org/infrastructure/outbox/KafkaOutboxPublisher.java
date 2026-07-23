package com.org.infrastructure.outbox;


import com.org.persistence.entities.OutboxEventEntity;
import com.org.persistence.repository.OutboxEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaOutboxPublisher {
    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaOutboxPublisher(
            OutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPendingEvent(){
        final List<OutboxEventEntity> events = outboxRepository.findPendingEvents(
                (Pageable) PageRequest.of(0, BATCH_SIZE)
        );

        for (OutboxEventEntity event : events) {

            publish(event);
        }

    }

    public void publish(OutboxEventEntity events) {
        try{
            kafkaTemplate
                    .send(events.getTopic(), events.getEventKey(), events.getPayload())
                    .get();

            events.markPublished();
            outboxRepository.save(events);
        } catch (InterruptedException | ExecutionException e) {
            events.markFailed(e.getMessage());
            outboxRepository.save(events);
        }
    }

}
