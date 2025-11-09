package by.javaguru.core.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductReserveCancelCommand(UUID orderId, UUID productId, BigDecimal price, Integer quantity) {
}
