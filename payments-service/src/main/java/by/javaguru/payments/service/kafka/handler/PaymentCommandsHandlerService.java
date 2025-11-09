package by.javaguru.payments.service.kafka.handler;

import by.javaguru.core.dto.Payment;
import by.javaguru.core.dto.command.PaymentsProcessCommand;
import by.javaguru.core.dto.events.PaymentsProcessFailedEvent;
import by.javaguru.core.dto.events.PaymentsProcessedEvent;
import by.javaguru.core.exceptions.CreditCardProcessorUnavailableException;
import by.javaguru.payments.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${payments.commands.topic.name}"
})
public class PaymentCommandsHandlerService {
    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String paymentsEventsTopicName;

    public PaymentCommandsHandlerService(PaymentService paymentService,
                                         KafkaTemplate<String, Object> kafkaTemplate,
                                         @Value("${payments.events.topic.name}") String paymentsEventsTopicName) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentsEventsTopicName = paymentsEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(PaymentsProcessCommand paymentsProcessCommand) {
        Payment payment = new Payment(
                paymentsProcessCommand.getOrderId(),
                paymentsProcessCommand.getProductId(),
                paymentsProcessCommand.getPrice(),
                paymentsProcessCommand.getProductQuantity()
        );

        try {
            Payment processed = paymentService.process(payment);
            kafkaTemplate.send(paymentsEventsTopicName, new PaymentsProcessedEvent(
                    processed.getOrderId(), processed.getId()
            ));
        } catch (CreditCardProcessorUnavailableException e) {
            logger.error(e.getLocalizedMessage(), e);
            PaymentsProcessFailedEvent paymentsProcessFailedEvent = new PaymentsProcessFailedEvent(
                    payment.getOrderId(), payment.getProductId(), payment.getProductPrice(), payment.getProductQuantity()
            );
            kafkaTemplate.send(paymentsEventsTopicName, paymentsProcessFailedEvent);
        }
    }
}
