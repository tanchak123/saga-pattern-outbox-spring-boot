package by.javaguru.orders.dao.jpa.repository;

import by.javaguru.core.types.OutboxStatus;
import by.javaguru.orders.dao.jpa.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
