package by.javaguru.orders.components.outbox;

import by.javaguru.core.types.OutboxStatus;
import by.javaguru.orders.dao.jpa.entity.OutboxEventEntity;
import by.javaguru.orders.dao.jpa.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${orders.events.topic.name}")
    private String topicName;

    public OutboxPublisher(OutboxEventRepository repository,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    public void processOutbox() {
        List<OutboxEventEntity> events = repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for (OutboxEventEntity event : events) {
            try {
                Class<?> clazz = Class.forName(event.getEventType());
                Object payloadObj = objectMapper.readValue(event.getPayload(), clazz);

                kafkaTemplate.send(topicName, payloadObj).get();

                event.setStatus(OutboxStatus.SENT);
                repository.save(event);

            } catch (Exception ex) {
                event.setStatus(OutboxStatus.FAILED);
                repository.save(event);
            }
        }
    }
}
