package by.javaguru.orders.service.kafka.handler;

import by.javaguru.core.dto.command.OrderApproveCommand;
import by.javaguru.core.dto.command.OrderRejectCommand;
import by.javaguru.core.dto.events.OrderApprovedEvent;
import by.javaguru.core.dto.events.OrderRejectedEvent;
import by.javaguru.orders.service.OrderService;
import by.javaguru.orders.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(topics = {
        "${orders.commands.topic.name}"
})
public class OrderCommandsHandlerService {
    private final OrderService orderService;
    private final String orderEventsTopicName;
    private final OutboxService outboxService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OrderCommandsHandlerService(OrderService orderService,
                                       @Value("${orders.events.topic.name}")String orderEventsTopicName,
                                       OutboxService outboxService) {
        this.orderService = orderService;
        this.outboxService = outboxService;
        this.orderEventsTopicName = orderEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(OrderApproveCommand command) {
        try {
            orderService.approveOrder(command.getOrderId());
            outboxService.saveEvent(new OrderApprovedEvent(command.getOrderId()), command.getOrderId().toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @KafkaHandler
    public void handleCommand(OrderRejectCommand command) {
        try {
            orderService.rejectOrder(command.orderId());
            //todo  there can be problem with kafka, need to learn Transactional Outbox Pattern and @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
            outboxService.saveEvent(new OrderRejectedEvent(command.orderId()), command.orderId().toString());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
