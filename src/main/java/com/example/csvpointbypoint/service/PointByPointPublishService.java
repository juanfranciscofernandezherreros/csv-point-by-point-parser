package com.example.csvpointbypoint.service;

import com.example.csvpointbypoint.avro.PointByPointKey;
import com.example.csvpointbypoint.avro.PointByPointValue;
import com.example.csvpointbypoint.mapper.PointByPointMessageMapper;
import com.example.csvpointbypoint.parser.PointByPointCsvParser;
import com.example.csvpointbypoint.validation.SafeCsvPathValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PointByPointPublishService {
    private static final int CHUNK_SIZE = 500;

    private final PointByPointCsvParser parser;
    private final PointByPointMessageMapper mapper;
    private final KafkaTemplate<PointByPointKey, PointByPointValue> kafka;
    private final SafeCsvPathValidator pathValidator;
    private final String topic;

    public PointByPointPublishService(PointByPointCsvParser parser, PointByPointMessageMapper mapper,
                                      KafkaTemplate<PointByPointKey, PointByPointValue> kafka,
                                      SafeCsvPathValidator pathValidator,
                                      @Value("${app.kafka.topics.parsed-point-by-point}") String topic) {
        this.parser = parser;
        this.mapper = mapper;
        this.kafka = kafka;
        this.pathValidator = pathValidator;
        this.topic = topic;
    }

    public void publishFile(String eventId, String filePath, Long expectedRowsHint) {
        PointByPointKey key = mapper.key(eventId);
        Path path = null;
        Long validatedExpectedRows = null;
        try {
            path = pathValidator.validate(filePath);
            Validation validation = validate(path);
            final long expectedRows = validation.rows();
            validatedExpectedRows = expectedRows;

            if (expectedRowsHint != null && expectedRowsHint.longValue() != expectedRows) {
                throw new IllegalStateException("CSV has " + expectedRows + " rows; expected " + expectedRowsHint);
            }

            send(key, mapper.control(eventId, "START", filePath, expectedRows, validation.matchId(), null));

            AtomicLong rowNumber = new AtomicLong();
            parser.parseInChunks(path, CHUNK_SIZE, rows -> rows.forEach(row ->
                    send(key, mapper.row(eventId, filePath, expectedRows, rowNumber.incrementAndGet(), row))));

            send(key, mapper.control(eventId, "COMPLETED", filePath, expectedRows, validation.matchId(), null));
        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            long safeExpected = validatedExpectedRows == null
                    ? Math.max(0L, expectedRowsHint == null ? 0L : expectedRowsHint)
                    : validatedExpectedRows;
            try {
                send(key, mapper.control(eventId, "FAILED", filePath, safeExpected, null, message));
            } catch (Exception ignored) {
                // Preserve the original parsing/validation failure if reporting it also fails.
            }
            throw new IllegalStateException("Unable to parse POINT_BY_POINT CSV: " + filePath, exception);
        }
    }

    private Validation validate(Path path) throws Exception {
        AtomicLong rows = new AtomicLong();
        AtomicReference<String> matchId = new AtomicReference<>();
        parser.parseInChunks(path, CHUNK_SIZE, chunk -> {
            for (var row : chunk) {
                if (matchId.get() == null) matchId.set(row.matchId());
                if (!matchId.get().equals(row.matchId())) {
                    throw new IllegalArgumentException("CSV contains more than one match_id");
                }
            }
            rows.addAndGet(chunk.size());
        });
        return new Validation(rows.get(), matchId.get());
    }

    private void send(PointByPointKey key, PointByPointValue value) {
        kafka.send(topic, key, value).join();
    }

    private record Validation(long rows, String matchId) {}
}
