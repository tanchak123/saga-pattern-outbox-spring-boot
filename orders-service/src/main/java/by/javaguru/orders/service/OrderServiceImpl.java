package by.javaguru.orders.service;

import by.javaguru.core.dto.Order;
import by.javaguru.core.dto.events.OrderCreatedEvent;
import by.javaguru.core.types.OrderStatus;
import by.javaguru.orders.dao.jpa.entity.OrderEntity;
import by.javaguru.orders.dao.jpa.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final String orderEventsTopicName;
    private final OutboxService outboxService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            @Value("${orders.events.topic.name}") String orderEventsTopicName, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.orderEventsTopicName = orderEventsTopicName;
        this.outboxService = outboxService;
    }

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        OrderCreatedEvent placeOrder = new OrderCreatedEvent(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity()
        );

        outboxService.saveEvent(placeOrder, placeOrder.getOrderId().toString());
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    @Transactional
    public Order approveOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        entity.setStatus(OrderStatus.APPROVED);
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    @Transactional
    public Order rejectOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        entity.setStatus(OrderStatus.REJECTED);
        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

}
