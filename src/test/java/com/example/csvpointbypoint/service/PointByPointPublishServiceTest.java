package com.example.csvpointbypoint.service;

import com.example.csvpointbypoint.avro.PointByPointKey;
import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.dto.PointByPointEventDTO;
import com.example.csvpointbypoint.mapper.PointByPointMessageMapper;
import com.example.csvpointbypoint.parser.PointByPointCsvParser;
import com.example.csvpointbypoint.validation.SafeCsvPathValidator;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PointByPointPublishServiceTest {

    @Test
    void sendsAllRowsInChunkBeforeAwaitingAcknowledgements() throws Exception {
        PointByPointCsvParser parser = mock(PointByPointCsvParser.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<PointByPointKey, PointByPointValue> kafka = mock(KafkaTemplate.class);
        SafeCsvPathValidator pathValidator = mock(SafeCsvPathValidator.class);
        PointByPointMessageMapper mapper = new PointByPointMessageMapper();
        Path path = Path.of("/data/pbp.csv");

        when(pathValidator.validate("/data/pbp.csv")).thenReturn(path);
        mockTwoPassParser(parser, path, rows());

        CompletableFuture<Object> firstRowAck = new CompletableFuture<>();
        CountDownLatch secondRowSent = new CountDownLatch(1);

        when(kafka.send(eq("point-by-point.parsed"), any(PointByPointKey.class), any(PointByPointValue.class)))
                .thenAnswer(invocation -> {
                    PointByPointValue value = invocation.getArgument(2);
                    if ("ROW".equals(value.getEventType()) && value.getRowNumber() == 1L) {
                        return firstRowAck;
                    }
                    if ("ROW".equals(value.getEventType()) && value.getRowNumber() == 2L) {
                        secondRowSent.countDown();
                    }
                    return CompletableFuture.completedFuture(null);
                });

        PointByPointPublishService service = new PointByPointPublishService(
                parser, mapper, kafka, pathValidator, "point-by-point.parsed");

        var executor = Executors.newSingleThreadExecutor();
        try {
            var publish = executor.submit(() -> service.publishFile("e1", "/data/pbp.csv", 2L));

            assertTrue(secondRowSent.await(1, TimeUnit.SECONDS),
                    "second ROW must be sent before the first ROW acknowledgement completes");

            firstRowAck.complete(null);
            publish.get(2, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        verify(kafka).send(
                eq("point-by-point.parsed"),
                any(PointByPointKey.class),
                org.mockito.ArgumentMatchers.argThat(value -> "COMPLETED".equals(value.getEventType())));
    }

    @Test
    void failedRowAcknowledgementPreventsCompleted() throws Exception {
        PointByPointCsvParser parser = mock(PointByPointCsvParser.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<PointByPointKey, PointByPointValue> kafka = mock(KafkaTemplate.class);
        SafeCsvPathValidator pathValidator = mock(SafeCsvPathValidator.class);
        PointByPointMessageMapper mapper = new PointByPointMessageMapper();
        Path path = Path.of("/data/pbp.csv");

        when(pathValidator.validate("/data/pbp.csv")).thenReturn(path);
        mockTwoPassParser(parser, path, rows().subList(0, 1));

        when(kafka.send(eq("point-by-point.parsed"), any(PointByPointKey.class), any(PointByPointValue.class)))
                .thenAnswer(invocation -> {
                    PointByPointValue value = invocation.getArgument(2);
                    if ("ROW".equals(value.getEventType())) {
                        return CompletableFuture.failedFuture(new KafkaException("broker unavailable"));
                    }
                    return CompletableFuture.completedFuture(null);
                });

        PointByPointPublishService service = new PointByPointPublishService(
                parser, mapper, kafka, pathValidator, "point-by-point.parsed");

        assertThrows(RuntimeException.class,
                () -> service.publishFile("e1", "/data/pbp.csv", 1L));

        verify(kafka, never()).send(
                eq("point-by-point.parsed"),
                any(PointByPointKey.class),
                org.mockito.ArgumentMatchers.argThat(value -> "COMPLETED".equals(value.getEventType())));
        verify(kafka).send(
                eq("point-by-point.parsed"),
                any(PointByPointKey.class),
                org.mockito.ArgumentMatchers.argThat(value -> "FAILED".equals(value.getEventType())));
    }

    private void mockTwoPassParser(
            PointByPointCsvParser parser,
            Path path,
            List<PointByPointEventDTO> rows) throws Exception {
        AtomicInteger invocation = new AtomicInteger();
        doAnswer(call -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<PointByPointEventDTO>> consumer = call.getArgument(2);
            consumer.accept(rows);
            invocation.incrementAndGet();
            return null;
        }).when(parser).parseInChunks(eq(path), eq(500), any());
    }

    private List<PointByPointEventDTO> rows() {
        return List.of(
                new PointByPointEventDTO("m1", "point_event", "Q1", 1, 2, 0, 2, 0,
                        "HOME", "2", "UP", true, false),
                new PointByPointEventDTO("m1", "point_event", "Q1", 2, 4, 0, 2, 0,
                        "HOME", "4", "UP", true, false));
    }
}
