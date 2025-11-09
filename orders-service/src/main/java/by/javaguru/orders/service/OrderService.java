package by.javaguru.orders.service;

import by.javaguru.core.dto.Order;

import java.util.UUID;

public interface OrderService {
    Order placeOrder(Order order);
    Order approveOrder(UUID orderId);
    Order rejectOrder(UUID orderId);

}
