package com.example.csvpointbypoint.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaErrorHandlingConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    @Bean
    KafkaErrorClassifier kafkaErrorClassifier() {
        return new KafkaErrorClassifier();
    }

    @Bean
    ProducerFactory<Object, Object> kafkaProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                properties,
                delegatingAvroSerializer(),
                delegatingAvroSerializer());
    }

    @Bean
    KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.dlt}") String dltTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dltTopic, -1));
        recoverer.setFailIfSendResultIsError(true);
        return recoverer;
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer,
            KafkaErrorClassifier classifier,
            @Value("${app.kafka.error.backoff-ms:1000}") long backoffMs,
            @Value("${app.kafka.error.max-attempts:3}") long maxAttempts) {

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

    DelegatingByTypeSerializer delegatingAvroSerializer() {
        Map<Class<?>, Serializer<?>> serializers = new LinkedHashMap<>();
        serializers.put(byte[].class, new ByteArraySerializer());
        serializers.put(Object.class, new KafkaAvroSerializer());
        return new DelegatingByTypeSerializer(serializers, true);
    }
}
