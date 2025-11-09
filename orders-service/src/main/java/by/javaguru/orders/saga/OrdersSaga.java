package by.javaguru.orders.saga;

import by.javaguru.core.dto.command.*;
import by.javaguru.core.dto.events.*;
import by.javaguru.core.types.OrderStatus;
import by.javaguru.orders.service.OrderHistoryService;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static by.javaguru.core.types.OrderStatus.*;

@Component
@KafkaListener(topics = {
        "${orders.events.topic.name}",
        "${products.events.topic.name}",
        "${payments.events.topic.name}"
})
public class OrdersSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productCommandsTopicName;
    private final String paymentsCommandsTopicName;
    private final String ordersCommandsTopicName;
    private final OrderHistoryService orderHistoryService;

    public OrdersSaga(KafkaTemplate<String, Object> kafkaTemplate,
                      @Value("${products.commands.topic.name}") String productsCommandsTopicName,
                      @Value("${payments.commands.topic.name}") String paymentsCommandsTopicName,
                      @Value("${orders.commands.topic.name}") String ordersCommandsTopicName,
                      OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.productCommandsTopicName = productsCommandsTopicName;
        this.paymentsCommandsTopicName = paymentsCommandsTopicName;
        this.ordersCommandsTopicName = ordersCommandsTopicName;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent orderCreatedEvent) {
        ProductReserveCommand productReserveCommand = new ProductReserveCommand(orderCreatedEvent.getOrderId(),
                orderCreatedEvent.getProductId(),
                orderCreatedEvent.getProductQuantity()
        );

        OrderStatus finalOrderStatus = CREATED;
        try {
            kafkaTemplate.send(productCommandsTopicName, productReserveCommand);
        } catch (Exception e) {
            finalOrderStatus = REJECTED;
        }

        orderHistoryService.add(productReserveCommand.getOrderId(), finalOrderStatus);
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderApprovedEvent orderApprovedEvent) {
        orderHistoryService.add(orderApprovedEvent.orderId(), APPROVED);

    }

    @KafkaHandler
    public void handleEvent(@Payload OrderRejectedEvent orderRejectedEvent) {
        orderHistoryService.add(orderRejectedEvent.orderId(), REJECTED);

    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent productReservedEvent) {
        PaymentsProcessCommand paymentsProcessCommand = new PaymentsProcessCommand(
                productReservedEvent.getOrderId(),
                productReservedEvent.getProductId(),
                productReservedEvent.getPrice(),
                productReservedEvent.getProductQuantity()
        );

        kafkaTemplate.send(paymentsCommandsTopicName, paymentsProcessCommand);

    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReserveFailedEvent productReserveFailedEvent) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("PaymentsProcessFailedEvent");

    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentsProcessedEvent paymentsProcessedEvent) {
        OrderApproveCommand orderApproveCommand = new OrderApproveCommand(paymentsProcessedEvent.getOrderId());
        kafkaTemplate.send(ordersCommandsTopicName, orderApproveCommand);

    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentsProcessFailedEvent paymentsProcessFailedEvent) {
        ProductReserveCancelCommand productReserveCommand = new ProductReserveCancelCommand(
                paymentsProcessFailedEvent.getOrderId(), paymentsProcessFailedEvent.getProductId(),
                paymentsProcessFailedEvent.getPrice(), paymentsProcessFailedEvent.getProductQuantity()
        );
        kafkaTemplate.send(productCommandsTopicName, productReserveCommand);

        OrderRejectCommand orderRejectCommand = new OrderRejectCommand(
                paymentsProcessFailedEvent.getOrderId()
        );

        kafkaTemplate.send(ordersCommandsTopicName, orderRejectCommand);
    }
}
