package by.javaguru.core.dto.events;

import java.util.UUID;

public record OrderRejectedEvent(UUID orderId) {
}
