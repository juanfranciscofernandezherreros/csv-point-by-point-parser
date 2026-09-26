package com.example.csvpointbypoint.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    @Bean
    KafkaErrorClassifier kafkaErrorClassifier() {
        return new KafkaErrorClassifier();
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            KafkaErrorClassifier classifier,
            @Value("${app.kafka.error.backoff-ms:1000}") long backoffMs,
            @Value("${app.kafka.error.max-attempts:3}") long maxAttempts,
            @Value("${app.kafka.topics.dlt}") String dltTopic) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dltTopic, record.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(backoffMs, Math.max(0, maxAttempts - 1)));
        handler.setClassifications(classifier.classifications(), true);
        handler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.warn("event=kafka_consumer_retry topic={} partition={} offset={} attempt={} exception={}",
                        record.topic(), record.partition(), record.offset(), deliveryAttempt,
                        exception.getClass().getSimpleName()));
        return handler;
    }
}
