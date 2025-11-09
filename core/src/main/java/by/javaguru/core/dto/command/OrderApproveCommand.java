package by.javaguru.core.dto.command;

import java.util.UUID;

public class OrderApproveCommand {
    private UUID orderId;

    public OrderApproveCommand(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderApproveCommand() {
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
