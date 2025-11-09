package by.javaguru.orders.service.kafka.handler;

import by.javaguru.core.dto.command.OrderApproveCommand;
import by.javaguru.core.dto.command.OrderRejectCommand;
import by.javaguru.core.dto.events.OrderApprovedEvent;
import by.javaguru.core.dto.events.OrderRejectedEvent;
import by.javaguru.orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${orders.commands.topic.name}"
})
public class OrderCommandsHandlerService {
    private final OrderService orderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopicName;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OrderCommandsHandlerService(OrderService orderService,
                                       KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${orders.events.topic.name}")String orderEventsTopicName) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopicName = orderEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(OrderApproveCommand command) {
        try {
            orderService.approveOrder(command.getOrderId());
            //todo  there can be problem with kafka, need to learn Transactional Outbox Pattern and @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
            kafkaTemplate.send(orderEventsTopicName, new OrderApprovedEvent(command.getOrderId()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @KafkaHandler
    public void handleCommand(OrderRejectCommand command) {
        try {
            orderService.rejectOrder(command.orderId());
            //todo  there can be problem with kafka, need to learn Transactional Outbox Pattern and @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
            kafkaTemplate.send(orderEventsTopicName, new OrderRejectedEvent(command.orderId()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
