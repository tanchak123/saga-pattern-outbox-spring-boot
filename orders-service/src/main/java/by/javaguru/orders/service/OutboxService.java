package by.javaguru.orders.service;

import by.javaguru.orders.dao.jpa.entity.OutboxEventEntity;
import by.javaguru.orders.dao.jpa.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(Object event, String aggregateId) {
        try {
            OutboxEventEntity entity = new OutboxEventEntity();
            entity.setAggregateId(aggregateId);
            entity.setEventType(event.getClass().getName());
            entity.setPayload(objectMapper.writeValueAsString(event));

            repository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
