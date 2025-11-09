package by.javaguru.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    private static final Integer TOPIC_REPLICATION_FACTOR = 3;
    private static final Integer TOPIC_PARTITIONS = 3;

    @Value("${orders.events.topic.name}")
    private String orderEventsTopicName;
    @Value("${orders.commands.topic.name}")
    private String orderCommandsTopicName;
    @Value("${payments.events.topic.name}")
    private String paymentsEventsTopicName;
    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;
    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;
    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;


    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic ordersEventsTopic() {
        return TopicBuilder.name(orderEventsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic ordersCommandsTopic() {
        return TopicBuilder.name(orderCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic paymentsEventsTopic() {
        return TopicBuilder.name(paymentsEventsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic paymentsCommandsTopic() {
        return TopicBuilder.name(paymentsCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic productsEventsTopic() {
        return TopicBuilder.name(productsEventsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic productsCommandsTopic() {
        return TopicBuilder.name(productsCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }

}
