package by.javaguru.products.service.kafka.handler;


import by.javaguru.core.dto.Product;
import by.javaguru.core.dto.command.ProductReserveCommand;
import by.javaguru.core.dto.command.ProductReserveCancelCommand;
import by.javaguru.core.dto.events.ProductReserveFailedEvent;
import by.javaguru.core.dto.events.ProductReservedEvent;
import by.javaguru.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${products.commands.topic.name}"
})
public class ProductCommandsHandlerService {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String productsEventsTopicName;

    public ProductCommandsHandlerService(ProductService productService,
                                         KafkaTemplate<String, Object> kafkaTemplate,
                                         @Value("${products.events.topic.name}") String productsEventsTopicName) {

        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productsEventsTopicName = productsEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(ProductReserveCommand productReserveCommand) {

        try {
            Product desiredProduct = new Product(productReserveCommand.getProductId(),
                    productReserveCommand.getProductQuantity()
            );
            Product reserved = productService.reserve(desiredProduct, productReserveCommand.getOrderId());
            ProductReservedEvent reservedEvent = new ProductReservedEvent(
                    productReserveCommand.getOrderId(), reserved.getId(), reserved.getPrice(), reserved.getQuantity()
            );
            kafkaTemplate.send(productsEventsTopicName, reservedEvent);
        } catch (Exception e) {
            ProductReserveFailedEvent productReserveFailedEvent = new ProductReserveFailedEvent(
                    productReserveCommand.getOrderId(),
                    productReserveCommand.getProductId(),
                    productReserveCommand.getProductQuantity()
            );
            kafkaTemplate.send(productsEventsTopicName, productReserveFailedEvent);
        }
    }


    @KafkaHandler
    public void handleCommand(ProductReserveCancelCommand productReserveCancelCommand) {
        productService.cancelReservation(
                productReserveCancelCommand.productId(), productReserveCancelCommand.quantity()
        );
    }
}
