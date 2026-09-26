package com.example.csvpointbypoint.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.SerializationUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaDeserializationDltTest {

    @Test
    void routesOriginalBytesFromDeserializationFailureToDlt() {
        byte[] malformedAvro = new byte[] {99, 98, 97, 96};
        RecordHeaders headers = new RecordHeaders();

        ErrorHandlingDeserializer<byte[]> deserializer =
                new ErrorHandlingDeserializer<>(new AlwaysFailingDeserializer());

        byte[] value = deserializer.deserialize("file.ready.point-by-point", headers, malformedAvro);

        assertThat(value).isNull();
        assertThat(headers.lastHeader(SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER)).isNotNull();

        @SuppressWarnings("unchecked")
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        DeadLetterPublishingRecoverer recoverer =
                new KafkaErrorHandlingConfig().deadLetterPublishingRecoverer(
                        template, "file.ready.point-by-point.DLT");

        ConsumerRecord<Object, Object> failedRecord =
                new ConsumerRecord<>("file.ready.point-by-point", 7, 42L, null, value);
        headers.forEach(failedRecord.headers()::add);

        recoverer.accept(failedRecord, new IllegalStateException("deserialization failed"));

        ArgumentCaptor<ProducerRecord<Object, Object>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(template).send(captor.capture());

        ProducerRecord<Object, Object> dltRecord = captor.getValue();
        assertThat(dltRecord.topic()).isEqualTo("file.ready.point-by-point.DLT");
        assertThat(dltRecord.partition()).isNull();
        assertThat(dltRecord.value()).isEqualTo(malformedAvro);
    }

    static class AlwaysFailingDeserializer implements Deserializer<byte[]> {
        @Override
        public byte[] deserialize(String topic, byte[] data) {
            throw new IllegalArgumentException("malformed avro");
        }

        @Override
        public byte[] deserialize(String topic, org.apache.kafka.common.header.Headers headers, byte[] data) {
            throw new IllegalArgumentException("malformed avro");
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration required.
        }
    }
}
